package com.project.ProjectS.service;

import com.project.ProjectS.entity.AnswerEvent;
import com.project.ProjectS.entity.Exam;
import com.project.ProjectS.entity.McqOption;
import com.project.ProjectS.entity.MockExam;
import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionAttribute;
import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.AttributeReviewDetailDTO;
import com.project.ProjectS.model.ExamAnswerDTO;
import com.project.ProjectS.model.ExamQuestionAnswerDTO;
import com.project.ProjectS.model.ExamReviewQuestionDTO;
import com.project.ProjectS.model.RuleConditionDTO;
import com.project.ProjectS.model.RuleEngineResponse;
import com.project.ProjectS.repository.AnswerEventRepository;
import com.project.ProjectS.repository.McqOptionRepository;
import com.project.ProjectS.repository.QuestionAttributeRepository;
import com.project.ProjectS.repository.QuestionRepository;
import com.project.ProjectS.repository.TableHeaderRepository;
import com.project.ProjectS.repository.TableNameRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scores a set of answered questions against the stored correct answers.
 * <p>
 * This logic was extracted verbatim from {@code ExamService.submitExam} so that
 * real exams and mock exams score submissions in exactly the same way. Both
 * {@link ExamService} and {@code MockExamService} delegate here.
 */
@Service
public class ExamScoringService {

    /**
     * Marks awarded and the maximum that could have been awarded, plus the
     * per-question breakdown (used by the exam review screen).
     */
    public record Score(
            double totalMarks,
            double maximumMarks,
            List<QuestionScore> questionScores) {

        public double percentage() {
            return maximumMarks == 0
                    ? 0
                    : (totalMarks / maximumMarks) * 100;
        }
    }

    /**
     * Whether a single question on the paper was answered fully correctly.
     */
    public record QuestionScore(
            Long questionId,
            boolean correct,
            double earnedMarks,
            double maxMarks) {
    }

    private final QuestionAttributeRepository questionAttributeRepository;
    private final McqOptionRepository mcqOptionRepository;
    private final RuleEngineService ruleEngineService;
    private final TableNameRepository tableNameRepository;
    private final TableHeaderRepository tableHeaderRepository;
    private final QuestionRepository questionRepository;
    private final AnswerEventRepository answerEventRepository;

    public ExamScoringService(
            QuestionAttributeRepository questionAttributeRepository,
            McqOptionRepository mcqOptionRepository,
            RuleEngineService ruleEngineService,
            TableNameRepository tableNameRepository,
            TableHeaderRepository tableHeaderRepository,
            QuestionRepository questionRepository,
            AnswerEventRepository answerEventRepository) {

        this.questionAttributeRepository = questionAttributeRepository;
        this.mcqOptionRepository = mcqOptionRepository;
        this.ruleEngineService = ruleEngineService;
        this.tableNameRepository = tableNameRepository;
        this.tableHeaderRepository = tableHeaderRepository;
        this.questionRepository = questionRepository;
        this.answerEventRepository = answerEventRepository;
    }

    /**
     * @param questionIds      the questions on the paper, in any order
     * @param submittedAnswers the candidate's answers (may be a subset of the paper)
     */
    public Score score(
            List<Long> questionIds,
            List<ExamQuestionAnswerDTO> submittedAnswers) {

        List<ExamQuestionAnswerDTO> answers =
                submittedAnswers == null ? List.of() : submittedAnswers;

        double totalMarks = 0.0;
        double maximumMarks = 0.0;
        List<QuestionScore> questionScores = new ArrayList<>();

        for (Long questionId : questionIds) {

            ExamQuestionAnswerDTO submittedQuestion =
                    answers.stream()
                            .filter(answer ->
                                    questionId.equals(answer.getQuestionId()))
                            .findFirst()
                            .orElse(null);

            if (submittedQuestion == null) {
                continue;
            }

            String questionType =
                    submittedQuestion.getQuestionType();

            double questionEarned = 0.0;
            double questionMax;

            if ("SINGLE_CHOICE".equalsIgnoreCase(questionType)
                    || "MULTIPLE_CHOICE".equalsIgnoreCase(questionType)) {

                questionMax = 1;
                maximumMarks += 1;

                if (checkMcqAnswer(submittedQuestion)) {
                    totalMarks += 1;
                    questionEarned = 1;
                }

            } else {

                List<QuestionAttribute> questionAttributes =
                        questionAttributeRepository.findByQuestion_QuestionId(questionId);

                long uniqueAttributeCount = questionAttributes.stream()
                        .filter(qa -> qa.getAttribute() != null)
                        .map(qa -> qa.getAttribute().getAttributeId())
                        .distinct()
                        .count();

                questionMax = uniqueAttributeCount;
                maximumMarks += uniqueAttributeCount;

                if (submittedQuestion.getAnswers() != null) {

                    Map<Long, List<ExamAnswerDTO>> answersByAttribute = new HashMap<>();

                    for (ExamAnswerDTO submittedAnswer : submittedQuestion.getAnswers()) {

                        if (submittedAnswer == null ||
                                submittedAnswer.getAnsweredData() == null) {
                            continue;
                        }

                        Long attributeId =
                                getLongValue(
                                        submittedAnswer.getAnsweredData()
                                                .get("attributeId")
                                );

                        if (attributeId == null) {
                            continue;
                        }

                        answersByAttribute
                                .computeIfAbsent(attributeId, key -> new ArrayList<>())
                                .add(submittedAnswer);
                    }

                    for (Map.Entry<Long, List<ExamAnswerDTO>> entry
                            : answersByAttribute.entrySet()) {

                        Long attributeId = entry.getKey();

                        List<ExamAnswerDTO> attributeAnswers = entry.getValue();

                        if (checkAccountingAttribute(
                                questionId,
                                attributeId,
                                attributeAnswers)) {

                            totalMarks += 1.0;
                            questionEarned += 1.0;
                        }
                    }
                }
            }

            questionScores.add(new QuestionScore(
                    questionId,
                    questionMax > 0 && questionEarned == questionMax,
                    questionEarned,
                    questionMax));
        }

        return new Score(totalMarks, maximumMarks, questionScores);
    }

    private boolean checkMcqAnswer(
            ExamQuestionAnswerDTO submittedQuestion) {

        if (submittedQuestion.getAnswers() == null
                || submittedQuestion.getAnswers().isEmpty()) {

            return false;
        }

        ExamAnswerDTO examAnswer =
                submittedQuestion.getAnswers().get(0);

        if (examAnswer.getAnsweredData() == null) {
            return false;
        }

        Map<String, Object> data = examAnswer.getAnsweredData();

        if (data.get("selectedAnswerId") == null && data.get("selectedAnswerIds") == null) {
            return false;
        }

        Set<Long> selectedIds = new HashSet<>(selectedOptionIds(data));

        List<McqOption> options =
                mcqOptionRepository
                        .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                submittedQuestion.getQuestionId()
                        );

        Set<Long> correctIds =
                options.stream()
                        .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                        .map(McqOption::getOptionId)
                        .collect(Collectors.toSet());

        return selectedIds.equals(correctIds);
    }

    // SINGLE_CHOICE sends a bare "selectedAnswerId"; MULTIPLE_CHOICE sends
    // "selectedAnswerIds" as a list. Normalises both into one list - shared by
    // scoring (checkMcqAnswer), persistence (persistAnswerInfo) and review
    // rehydration (buildReviewFromAnswerInfo).
    private List<Long> selectedOptionIds(Map<String, Object> data) {

        Object selectedAnswerIds = data.get("selectedAnswerIds");
        Object selectedAnswerId = data.get("selectedAnswerId");

        List<?> raw;

        if (selectedAnswerIds instanceof List<?> list) {
            raw = list;
        } else if (selectedAnswerId != null) {
            raw = List.of(selectedAnswerId);
        } else {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();

        for (Object value : raw) {
            Long id = getLongValue(value);
            if (id != null) {
                ids.add(id);
            }
        }

        return ids;
    }


    private boolean checkAccountingAttribute(
            Long questionId,
            Long attributeId,
            List<ExamAnswerDTO> submittedAnswers) {

        if (submittedAnswers == null || submittedAnswers.isEmpty()) {
            return false;
        }

        // Get QuestionAttribute
        QuestionAttribute questionAttribute =
                questionAttributeRepository
                        .findByQuestion_QuestionId(questionId)
                        .stream()
                        .filter(qa ->
                                qa.getAttribute() != null
                                        && attributeId.equals(
                                        qa.getAttribute().getAttributeId()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (questionAttribute == null) {
            return false;
        }

        // Get Rule Engine
        List<RuleEngineResponse> rules =
                ruleEngineService.getRuleEngineByAttributeId(attributeId);

        if (rules == null || rules.isEmpty()) {
            return false;
        }

        /*
         * Every submitted answer must match
         * one of the Rule Engine conditions.
         */
        for (ExamAnswerDTO submittedAnswer : submittedAnswers) {

            if (!matchesAnyRuleCondition(submittedAnswer, rules, questionAttribute)) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesAnyRuleCondition(
            ExamAnswerDTO submittedAnswer,
            List<RuleEngineResponse> rules,
            QuestionAttribute questionAttribute) {

        for (RuleEngineResponse rule : rules) {

            if (matchesCondition(submittedAnswer, rule.getCondition1(), questionAttribute)
                    || matchesCondition(submittedAnswer, rule.getCondition2(), questionAttribute)
                    || matchesCondition(submittedAnswer, rule.getCondition3(), questionAttribute)
                    || matchesCondition(submittedAnswer, rule.getCondition4(), questionAttribute)) {

                return true;
            }
        }

        return false;
    }

    private boolean isValidCondition(RuleConditionDTO condition) {

        if (condition == null) {
            return false;
        }

        return condition.getArithmetic() != null
                && condition.getTableId() != null
                && condition.getHeaderId() != null
                && condition.getAmountPosition() != null;
    }

    private boolean matchesCondition(
            ExamAnswerDTO submittedAnswer,
            RuleConditionDTO condition,
            QuestionAttribute questionAttribute) {

        if (submittedAnswer == null ||
                submittedAnswer.getAnsweredData() == null ||
                condition == null) {

            return false;
        }

        if (!isValidCondition(condition)) {
            return false;
        }

        Map<String, Object> data =
                submittedAnswer.getAnsweredData();

        String tableName =
                data.get("tableName") != null
                        ? data.get("tableName").toString()
                        : null;

        String headerName =
                data.get("headerName") != null
                        ? data.get("headerName").toString()
                        : null;

        String submittedArithmetic =
                data.get("arithmetic") != null
                        ? data.get("arithmetic").toString()
                        : null;

        BigDecimal submittedAmount =
                getBigDecimalValue(data.get("amount"));

        if (tableName == null ||
                headerName == null ||
                submittedArithmetic == null ||
                submittedAmount == null) {

            return false;
        }

        // Frontend name -> database ID
        Long submittedTableId =
                getTableIdByName(tableName);

        Long submittedHeaderId =
                getHeaderIdByName(headerName);

        if (submittedTableId == null ||
                submittedHeaderId == null) {

            return false;
        }

        // Compare table
        if (!submittedTableId.equals(condition.getTableId())) {
            return false;
        }

        // Compare header
        if (!submittedHeaderId.equals(condition.getHeaderId())) {
            return false;
        }

        // Compare arithmetic
        if (!submittedArithmetic.trim()
                .equalsIgnoreCase(
                        condition.getArithmetic().trim())) {

            return false;
        }

        // Get expected amount from QuestionAttribute
        BigDecimal expectedAmount =
                getExpectedAmount(
                        condition.getAmountPosition(),
                        questionAttribute
                );

        if (expectedAmount == null) {
            return false;
        }

        return expectedAmount.compareTo(submittedAmount) == 0;
    }


    private Long getTableIdByName(String tableName) {

        if (tableName == null) {
            return null;
        }

        String normalizedName =
                tableName
                        .trim()
                        .replaceAll("\\s+", " ");

        return tableNameRepository.findAll()
                .stream()
                .filter(table -> table.getName() != null)
                .filter(table ->
                        table.getName()
                                .trim()
                                .replaceAll("\\s+", " ")
                                .equalsIgnoreCase(normalizedName)
                )
                .map(TableName::getTableNameId)
                .findFirst()
                .orElse(null);
    }

    private Long getHeaderIdByName(String headerName) {

        if (headerName == null) {
            return null;
        }

        String normalizedName =
                headerName
                        .trim()
                        .replaceAll("\\s+", " ");

        return tableHeaderRepository.findAll()
                .stream()
                .filter(header -> header.getName() != null)
                .filter(header ->
                        header.getName()
                                .trim()
                                .replaceAll("\\s+", " ")
                                .equalsIgnoreCase(normalizedName)
                )
                .map(TableHeader::getHeaderId)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal getExpectedAmount(
            String amountPosition,
            QuestionAttribute questionAttribute) {

        if (amountPosition == null) {
            return null;
        }

        if ("amount".equalsIgnoreCase(amountPosition)
                || "amount1".equalsIgnoreCase(amountPosition)
                || "1".equalsIgnoreCase(amountPosition)) {

            return questionAttribute.getAmount();
        }

        if ("amount2".equalsIgnoreCase(amountPosition)
                || "2".equalsIgnoreCase(amountPosition)) {

            return questionAttribute.getAmount2();
        }

        return null;
    }

    private Long getLongValue(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal getBigDecimalValue(Object value) {

        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Saves each answer line's human-readable "what did the student do" line
     * (built client-side, the same way the practice flow's QuestionPage
     * builds AnswerEvent.userAnswer) into AnswerEvent.description. Only
     * answer lines that actually carry an "info" key are written - MCQ never
     * sends one, so this naturally only ever touches Journal/Dropdown/
     * Drag-and-drop answers. MCQ lines carry no "info" - instead, each
     * selected option gets its own row with AnswerEvent.optionId set (no
     * parsing needed, unlike the other types, since there's a column for it).
     * Every row also gets AnswerEvent.isCorrect stamped: for Journal/
     * Dropdown/Drag-and-drop lines, each line is judged independently against
     * the Rule Engine (via {@link #matchesAnyRuleCondition}) - two lines of
     * the same attribute can disagree, one right, one wrong. This is
     * deliberately more granular than the actual exam score: {@link #score}/
     * {@link #checkAccountingAttribute} still requires every line of an
     * attribute to match before marks are awarded for it, unchanged. The
     * review screen re-aggregates these per-line flags back into a
     * per-attribute verdict at read time (see {@link #buildReviewFromAnswerInfo})
     * rather than trusting a pre-aggregated flag here. For MCQ, there's no
     * attribute to split by, so every option gets the per-question verdict
     * from {@code questionScores} (the same {@link #score} call the caller
     * already made to grade the submission). marks is left unset, so none of
     * this can affect the practice "Total Score" widget. Pass exactly one of
     * {@code exam} / {@code mockExam}. Retaking the same paper deletes every
     * EXAM_SUBMIT row this user has for that exam (or mock exam) up front -
     * one delete for the whole attempt, not per question - so answer_events
     * only ever holds the current attempt's data, never a past one.
     */
    public void persistAnswerInfo(
            User user,
            Exam exam,
            MockExam mockExam,
            List<ExamQuestionAnswerDTO> submittedAnswers,
            List<QuestionScore> questionScores) {

        if (exam != null) {
            answerEventRepository.deleteByUser_UserIdAndExam_ExamIdAndEventType(
                    user.getUserId(), exam.getExamId(), "EXAM_SUBMIT");
        } else {
            answerEventRepository.deleteByUser_UserIdAndMockExam_MockExamIdAndEventType(
                    user.getUserId(), mockExam.getMockExamId(), "EXAM_SUBMIT");
        }

        Map<Long, Boolean> correctByQuestionId = new HashMap<>();
        for (QuestionScore questionScore : questionScores) {
            correctByQuestionId.put(questionScore.questionId(), questionScore.correct());
        }

        List<ExamQuestionAnswerDTO> answers =
                submittedAnswers == null ? List.of() : submittedAnswers;

        for (ExamQuestionAnswerDTO submittedQuestion : answers) {

            if (submittedQuestion.getAnswers() == null) {
                continue;
            }

            Question question =
                    questionRepository.findById(submittedQuestion.getQuestionId()).orElse(null);

            if (question == null) {
                continue;
            }

            Boolean questionCorrect = correctByQuestionId.get(submittedQuestion.getQuestionId());

            List<QuestionAttribute> questionAttributes =
                    questionAttributeRepository.findByQuestion_QuestionId(
                            submittedQuestion.getQuestionId());

            // Caches the Rule Engine lookup per attribute for this question,
            // since several submitted lines can share the same attribute.
            Map<Long, List<RuleEngineResponse>> rulesByAttributeId = new HashMap<>();

            for (ExamAnswerDTO answerLine : submittedQuestion.getAnswers()) {

                if (answerLine == null || answerLine.getAnsweredData() == null) {
                    continue;
                }

                Map<String, Object> data = answerLine.getAnsweredData();
                Object info = data.get("info");

                if (info != null) {

                    Long attributeId = getLongValue(data.get("attributeId"));

                    TableAttribute attribute = attributeId == null
                            ? null
                            : questionAttributes.stream()
                                    .map(QuestionAttribute::getAttribute)
                                    .filter(a -> a != null && attributeId.equals(a.getAttributeId()))
                                    .findFirst()
                                    .orElse(null);

                    QuestionAttribute questionAttribute = attributeId == null
                            ? null
                            : questionAttributes.stream()
                                    .filter(qa -> qa.getAttribute() != null
                                            && attributeId.equals(qa.getAttribute().getAttributeId()))
                                    .findFirst()
                                    .orElse(null);

                    // Each line is judged on its own here - not aggregated
                    // with its attribute's other lines, unlike the actual
                    // exam score (checkAccountingAttribute, unchanged) which
                    // still requires every line of an attribute to match
                    // before any marks are awarded for it.
                    Boolean lineCorrect = null;

                    if (attributeId != null && questionAttribute != null) {

                        List<RuleEngineResponse> rules = rulesByAttributeId.computeIfAbsent(
                                attributeId, ruleEngineService::getRuleEngineByAttributeId);

                        lineCorrect = rules != null && !rules.isEmpty()
                                && matchesAnyRuleCondition(answerLine, rules, questionAttribute);
                    }

                    AnswerEvent event = new AnswerEvent();
                    event.setUser(user);
                    event.setQuestion(question);
                    event.setAttribute(attribute);
                    event.setEventType("EXAM_SUBMIT");
                    event.setDescription(info.toString());
                    event.setIsCorrect(lineCorrect);
                    event.setExam(exam);
                    event.setMockExam(mockExam);

                    answerEventRepository.save(event);
                    continue;
                }

                for (Long optionId : selectedOptionIds(data)) {

                    AnswerEvent event = new AnswerEvent();
                    event.setUser(user);
                    event.setQuestion(question);
                    event.setOptionId(optionId);
                    event.setEventType("EXAM_SUBMIT");
                    event.setIsCorrect(questionCorrect);
                    event.setExam(exam);
                    event.setMockExam(mockExam);

                    answerEventRepository.save(event);
                }
            }
        }
    }

    // Reverses persistAnswerInfo's exact wording - only ever parses what this
    // service itself wrote, so any drift between the two must be kept in
    // sync by hand.
    private static final Pattern ANSWER_INFO_PATTERN =
            Pattern.compile("^attempted to (.+) on (.+) of (.+)\\.$");

    private record ParsedAnswerInfo(
            String arithmetic, String headerName, String tableName) {
    }

    private ParsedAnswerInfo parseAnswerInfo(String description) {

        if (description == null) {
            return null;
        }

        Matcher matcher = ANSWER_INFO_PATTERN.matcher(description.trim());

        if (!matcher.matches()) {
            return null;
        }

        String arithmetic =
                "SUBTRACT".equalsIgnoreCase(matcher.group(1)) ? "less" : "add";

        return new ParsedAnswerInfo(arithmetic, matcher.group(2), matcher.group(3));
    }

    // Same normalisation the frontend's questionTypeOf.js applies - the
    // question_type table stores display-cased labels ("Journal", "DropDown",
    // "Drag And Drop"), the review screen switches on canonical tokens.
    private String normalizeQuestionType(String raw) {

        if (raw == null) {
            return null;
        }

        String token = raw.trim().toUpperCase().replaceAll("[\\s-]+", "_");

        if (token.isEmpty()) {
            return null;
        }
        if (token.contains("JOURNAL")) {
            return "JOURNAL";
        }
        if (token.contains("DRAG") && token.contains("DROP")) {
            return "DRAG_AND_DROP";
        }
        if (token.contains("DROP") && token.contains("DOWN")) {
            return "DROPDOWN";
        }
        if (token.contains("MULTIPLE") && token.contains("CHOICE")) {
            return "MULTIPLE_CHOICE";
        }
        if (token.contains("SINGLE") && token.contains("CHOICE")) {
            return "SINGLE_CHOICE";
        }

        return token;
    }

    /**
     * Rebuilds the Exam Review screen's Journal/Dropdown/Drag-and-drop
     * answers from AnswerEvent.description, and MCQ answers from
     * AnswerEvent.optionId (both written by {@link #persistAnswerInfo}).
     * MCQ questions also carry {@code correctOptionIds}, looked up fresh
     * here - never sent on the live exam-taking payload. Pass exactly one of
     * {@code examId} / {@code mockExamId} - events are scoped directly to
     * that attempt, so a question shared across two papers can never leak
     * into the wrong one's review, and persistAnswerInfo's delete-then-insert
     * per attempt means only the latest submission is ever found here.
     */
    public List<ExamReviewQuestionDTO> buildReviewFromAnswerInfo(
            Long userId, Long examId, Long mockExamId) {

        List<AnswerEvent> events = examId != null
                ? answerEventRepository.findByUser_UserIdAndExam_ExamIdAndEventType(
                        userId, examId, "EXAM_SUBMIT")
                : answerEventRepository.findByUser_UserIdAndMockExam_MockExamIdAndEventType(
                        userId, mockExamId, "EXAM_SUBMIT");

        Map<Long, List<AnswerEvent>> eventsByQuestion = events.stream()
                .collect(Collectors.groupingBy(event -> event.getQuestion().getQuestionId()));

        List<ExamReviewQuestionDTO> reviewQuestions = new ArrayList<>();

        for (Map.Entry<Long, List<AnswerEvent>> entry : eventsByQuestion.entrySet()) {

            Long questionId = entry.getKey();
            List<AnswerEvent> questionEvents = entry.getValue();

            String questionType = normalizeQuestionType(
                    questionEvents.get(0).getQuestion().getQuestionType() != null
                            ? questionEvents.get(0).getQuestion().getQuestionType().getQuestionType()
                            : null);

            boolean isMcq = "SINGLE_CHOICE".equals(questionType)
                    || "MULTIPLE_CHOICE".equals(questionType);

            List<ExamAnswerDTO> answers;
            List<Long> correctOptionIds = List.of();

            if (isMcq) {

                List<Long> selectedIds = questionEvents.stream()
                        .map(AnswerEvent::getOptionId)
                        .filter(Objects::nonNull)
                        .toList();

                Map<String, Object> answeredData = new HashMap<>();
                if ("MULTIPLE_CHOICE".equals(questionType)) {
                    answeredData.put("selectedAnswerIds", selectedIds);
                } else {
                    answeredData.put(
                            "selectedAnswerId",
                            selectedIds.isEmpty() ? null : selectedIds.get(0));
                }

                ExamAnswerDTO answer = new ExamAnswerDTO();
                answer.setAnsweredData(answeredData);
                answers = List.of(answer);

                correctOptionIds = mcqOptionRepository
                        .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(questionId)
                        .stream()
                        .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                        .map(McqOption::getOptionId)
                        .toList();

            } else {

                List<QuestionAttribute> questionAttributes =
                        questionAttributeRepository.findByQuestion_QuestionId(questionId);

                // The status shown per line is the whole attribute's verdict,
                // not just this one line's: how many answer_events rows does
                // this attribute have for this attempt, and are all of them
                // individually marked is_correct? Recomputed here from the
                // stored per-line flags rather than trusting a pre-aggregated
                // one, so it always reflects exactly what's in the table.
                Map<Long, List<AnswerEvent>> eventsByAttributeId = questionEvents.stream()
                        .filter(event -> event.getAttribute() != null)
                        .collect(Collectors.groupingBy(
                                event -> event.getAttribute().getAttributeId()));

                Map<Long, Boolean> attributeAllCorrect = new HashMap<>();
                for (Map.Entry<Long, List<AnswerEvent>> attributeEntry : eventsByAttributeId.entrySet()) {
                    boolean allCorrect = attributeEntry.getValue().stream()
                            .allMatch(event -> Boolean.TRUE.equals(event.getIsCorrect()));
                    attributeAllCorrect.put(attributeEntry.getKey(), allCorrect);
                }

                List<ExamAnswerDTO> parsedAnswers = new ArrayList<>();

                for (AnswerEvent event : questionEvents) {

                    ParsedAnswerInfo parsed = parseAnswerInfo(event.getDescription());

                    if (parsed == null) {
                        continue;
                    }

                    Long attributeId = event.getAttribute() != null
                            ? event.getAttribute().getAttributeId()
                            : null;

                    BigDecimal amount = questionAttributes.stream()
                            .filter(qa -> qa.getAttribute() != null
                                    && qa.getAttribute().getAttributeId().equals(attributeId))
                            .map(QuestionAttribute::getAmount)
                            .findFirst()
                            .orElse(null);

                    Map<String, Object> answeredData = new HashMap<>();
                    answeredData.put("tableName", parsed.tableName());
                    answeredData.put("headerName", parsed.headerName());
                    answeredData.put("attributeId", attributeId);
                    answeredData.put("arithmetic", parsed.arithmetic());
                    answeredData.put("amount", amount);
                    answeredData.put(
                            "status",
                            Boolean.TRUE.equals(attributeAllCorrect.get(attributeId))
                                    ? "correct"
                                    : "wrong");

                    ExamAnswerDTO answer = new ExamAnswerDTO();
                    answer.setAnsweredData(answeredData);
                    parsedAnswers.add(answer);
                }

                answers = parsedAnswers;
            }

            ExamQuestionAnswerDTO reconstructed = new ExamQuestionAnswerDTO();
            reconstructed.setQuestionId(questionId);
            reconstructed.setQuestionType(questionType);
            reconstructed.setAnswers(answers);

            boolean correct = score(List.of(questionId), List.of(reconstructed))
                    .questionScores()
                    .stream()
                    .findFirst()
                    .map(QuestionScore::correct)
                    .orElse(false);

            ExamReviewQuestionDTO dto = new ExamReviewQuestionDTO();
            dto.setQuestionId(questionId);
            dto.setQuestionType(questionType);
            dto.setCorrect(correct);
            dto.setAnswers(answers);
            dto.setCorrectOptionIds(correctOptionIds);

            reviewQuestions.add(dto);
        }

        return reviewQuestions;
    }

    /**
     * What the review screen shows when a trial-balance / transaction row is
     * clicked: the attribute's Rule Engine hint text(s), plus each wrong line
     * this student submitted for it on this attempt - the individually
     * mis-matched answer_events rows, read straight off their stored
     * description, not a synthesised explanation of what was missed. Pass
     * exactly one of {@code examId} / {@code mockExamId}.
     */
    public AttributeReviewDetailDTO buildAttributeReviewDetail(
            Long userId, Long examId, Long mockExamId, Long questionId, Long attributeId) {

        List<AnswerEvent> wrongEvents = examId != null
                ? answerEventRepository
                        .findByUser_UserIdAndExam_ExamIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventTypeAndIsCorrectFalse(
                                userId, examId, questionId, attributeId, "EXAM_SUBMIT")
                : answerEventRepository
                        .findByUser_UserIdAndMockExam_MockExamIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventTypeAndIsCorrectFalse(
                                userId, mockExamId, questionId, attributeId, "EXAM_SUBMIT");

        List<String> mistakes = wrongEvents.stream()
                .map(AnswerEvent::getDescription)
                .filter(description -> description != null && !description.isBlank())
                .toList();

        List<RuleEngineResponse> rules = ruleEngineService.getRuleEngineByAttributeId(attributeId);

        List<String> hints = rules == null
                ? List.of()
                : rules.stream()
                        .flatMap(rule -> Stream.of(
                                rule.getCondition1(), rule.getCondition2(),
                                rule.getCondition3(), rule.getCondition4()))
                        .filter(Objects::nonNull)
                        .map(RuleConditionDTO::getInformation)
                        .filter(information -> information != null && !information.isBlank())
                        .distinct()
                        .toList();

        AttributeReviewDetailDTO detail = new AttributeReviewDetailDTO();
        detail.setHints(hints);
        detail.setMistakes(mistakes);
        return detail;
    }
}
