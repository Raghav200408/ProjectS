package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionService questionService;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionAttributeRepository questionAttributeRepository;
    private final ExamResultRepository examResultRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final McqOptionRepository mcqOptionRepository;
    private final RuleEngineService ruleEngineService;
    private final UserRepository userRepository;
    private final TableNameRepository tableNameRepository;
    private final TableHeaderRepository tableHeaderRepository;

    public ExamService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamResultRepository examResultRepository,
            CollegeRepository collegeRepository,
            BranchRepository branchRepository,
            CourseRepository courseRepository,
            SectionRepository sectionRepository,
            ChapterRepository chapterRepository,
            QuestionRepository questionRepository,
            QuestionAttributeRepository questionAttributeRepository,
            QuestionService questionService,
            McqQuestionRepository mcqQuestionRepository,
            McqOptionRepository mcqOptionRepository,
            RuleEngineService ruleEngineService,
            UserRepository userRepository,
            TableNameRepository tableNameRepository,
            TableHeaderRepository tableHeaderRepository) {

        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.chapterRepository = chapterRepository;
        this.questionRepository = questionRepository;
        this.questionAttributeRepository = questionAttributeRepository;
        this.questionService = questionService;
        this.examResultRepository = examResultRepository;
        this.mcqQuestionRepository = mcqQuestionRepository;
        this.mcqOptionRepository = mcqOptionRepository;
        this.ruleEngineService = ruleEngineService;
        this.userRepository = userRepository;
        this.tableNameRepository = tableNameRepository;
        this.tableHeaderRepository = tableHeaderRepository;

    }


    @Transactional
    public ExamResponseDTO createExam(ExamRequestDTO request) {

        College college = collegeRepository
                .findById(request.getCollegeId())
                .orElseThrow(() ->
                        new RuntimeException("College not found"));

        Branch branch = branchRepository
                .findById(request.getBranchId())
                .orElseThrow(() ->
                        new RuntimeException("Branch not found"));

        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found"));

        Section section = sectionRepository
                .findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found"));


        List<Chapter> chapters =
                chapterRepository.findAllById(
                        request.getChapterIds()
                );


        if (chapters.size() != request.getChapterIds().size()) {
            throw new RuntimeException(
                    "One or more chapters not found"
            );
        }


        // Create Exam
        Exam exam = new Exam();

        exam.setExamName(request.getExamName());

        exam.setCollege(college);

        exam.setBranch(branch);

        exam.setCourse(course);

        exam.setSection(section);

        exam.setChapters(chapters);

        exam.setStartDate(request.getStartDate());

        exam.setEndDate(request.getEndDate());

        exam.setPassPercentage(request.getPassPercentage());

        exam.setActiveRow(true);

        exam.setRowStatus(1);


        Exam savedExam =
                examRepository.save(exam);


        return convertToResponse(savedExam);
    }


    public List<ExamResponseDTO> getAllExams() {

        return examRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    public ExamResponseDTO getExamById(Long examId) {

        Exam exam = examRepository
                .findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: "
                                        + examId
                        ));

        return convertToResponse(exam);
    }


    public ExamResponseDTO updateExam(
            Long examId,
            ExamRequestDTO request) {

        Exam exam = examRepository
                .findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: "
                                        + examId
                        ));


        College college = collegeRepository
                .findById(request.getCollegeId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "College not found"
                        ));


        Branch branch = branchRepository
                .findById(request.getBranchId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Branch not found"
                        ));


        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found"
                        ));


        Section section = sectionRepository
                .findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Section not found"
                        ));


        List<Chapter> chapters =
                chapterRepository.findAllById(
                        request.getChapterIds()
                );


        if (chapters.size() != request.getChapterIds().size()) {
            throw new RuntimeException(
                    "One or more chapters not found"
            );
        }


        exam.setExamName(request.getExamName());

        exam.setCollege(college);

        exam.setBranch(branch);

        exam.setCourse(course);

        exam.setSection(section);

        exam.setChapters(chapters);

        exam.setStartDate(request.getStartDate());

        exam.setEndDate(request.getEndDate());

        exam.setPassPercentage(request.getPassPercentage());


        Exam updatedExam =
                examRepository.save(exam);


        return convertToResponse(updatedExam);
    }


    @Transactional
    public void deleteExam(Long examId) {

        if (!examRepository.existsById(examId)) {

            throw new RuntimeException(
                    "Exam not found with id: "
                            + examId
            );
        }

        examRepository.deleteById(examId);
    }

    public ExamSubmitResponseDTO submitExam(
            Long examId,
            ExamSubmitRequestDTO request) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found with id: " + examId)
                );

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + request.getUserId())
                );

        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExam_ExamId(examId);

        double totalMarks = 0.0;
        double maximumMarks = 0.0;

        for (ExamQuestion examQuestion : examQuestions) {

            Long questionId =
                    examQuestion.getQuestion().getQuestionId();

            ExamQuestionAnswerDTO submittedQuestion =
                    request.getAnswers()
                            .stream()
                            .filter(answer ->
                                    answer.getQuestionId().equals(questionId)
                            )
                            .findFirst()
                            .orElse(null);

            if (submittedQuestion == null) {
                continue;
            }

            String questionType =
                    submittedQuestion.getQuestionType();

            if ("SINGLE_CHOICE".equalsIgnoreCase(questionType)
                    || "MULTIPLE_CHOICE".equalsIgnoreCase(questionType)) {

                maximumMarks += 1;

                if (checkMcqAnswer(submittedQuestion)) {
                    totalMarks += 1;
                }

            } else {
                List<QuestionAttribute> questionAttributes =
                        questionAttributeRepository.findByQuestion_QuestionId(questionId);

                long uniqueAttributeCount = questionAttributes.stream()
                        .filter(qa -> qa.getAttribute() != null)
                        .map(qa -> qa.getAttribute().getAttributeId())
                        .distinct()
                        .count();

                maximumMarks += uniqueAttributeCount;

                if (submittedQuestion.getAnswers() == null) {
                    continue;
                }

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
                    }
                }


            }
        }

        double percentage = maximumMarks == 0
                ? 0
                : (totalMarks / maximumMarks) * 100;

        ExamResult result = new ExamResult();

        result.setExam(exam);
        result.setUser(user);
        result.setTotalMarks(totalMarks);
        result.setPercentage(percentage);

        examResultRepository.save(result);

        ExamSubmitResponseDTO response =
                new ExamSubmitResponseDTO();

        response.setExamId(examId);
        response.setUserId(request.getUserId());
        response.setTotalMarks(totalMarks);
        response.setPercentage(percentage);

        return response;
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

        Object selectedObject =
                examAnswer.getAnsweredData().get("selectedAnswerId");

        if (selectedObject == null) {
            selectedObject =
                    examAnswer.getAnsweredData().get("selectedAnswerIds");
        }

        if (!(selectedObject instanceof List<?> selectedList)) {
            return false;
        }

        Set<Long> selectedIds = new HashSet<>();

        for (Object value : selectedList) {

            if (value instanceof Number number) {
                selectedIds.add(number.longValue());
            } else {
                selectedIds.add(Long.valueOf(value.toString()));
            }
        }

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

            boolean answerMatched = false;

            for (RuleEngineResponse rule : rules) {

                if (matchesCondition(
                        submittedAnswer,
                        rule.getCondition1(),
                        questionAttribute)) {

                    answerMatched = true;
                    break;
                }

                if (matchesCondition(
                        submittedAnswer,
                        rule.getCondition2(),
                        questionAttribute)) {

                    answerMatched = true;
                    break;
                }

                if (matchesCondition(
                        submittedAnswer,
                        rule.getCondition3(),
                        questionAttribute)) {

                    answerMatched = true;
                    break;
                }

                if (matchesCondition(
                        submittedAnswer,
                        rule.getCondition4(),
                        questionAttribute)) {

                    answerMatched = true;
                    break;
                }
            }

            if (!answerMatched) {
                return false;
            }
        }

        return true;
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

    private String getNormalizedString(Object value) {

        if (value == null) {
            return null;
        }

        String text =
                value.toString()
                        .trim()
                        .replaceAll("\\s+", " ");

        return text.isEmpty() ? null : text;
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

    @Transactional
    public void addQuestionsToExam(
            Long examId,
            AddExamQuestionsRequestDTO request) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));


        List<Long> examChapterIds = exam.getChapters()
                .stream()
                .map(Chapter::getChapterId)
                .toList();


        List<Question> availableQuestions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
                                exam.getCourse().getCourseId(),
                                examChapterIds
                        );

        List<Long> availableQuestionIds =
                availableQuestions.stream()
                        .map(Question::getQuestionId)
                        .toList();

        for (Long questionId : request.getQuestionIds()) {

            // Make sure question belongs to exam's course + chapters
            if (!availableQuestionIds.contains(questionId)) {
                throw new RuntimeException(
                        "Question " + questionId +
                                " does not belong to the selected chapters of this exam"
                );
            }

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Question not found with id: " + questionId
                            ));

            // Prevent duplicate question
            boolean alreadyExists =
                    examQuestionRepository
                            .existsByExam_ExamIdAndQuestion_QuestionId(
                                    examId,
                                    questionId
                            );

            if (alreadyExists) {
                continue;
            }

            ExamQuestion examQuestion = new ExamQuestion();

            examQuestion.setExam(exam);
            examQuestion.setQuestion(question);

            examQuestionRepository.save(examQuestion);
        }
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getAvailableQuestions(Long examId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));


        List<Long> examChapterIds = exam.getChapters()
                .stream()
                .map(Chapter::getChapterId)
                .toList();


        List<Question> availableQuestions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
                                exam.getCourse().getCourseId(),
                                examChapterIds
                        );


        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExam_ExamId(examId);

        List<Long> addedQuestionIds =
                examQuestions.stream()
                        .map(examQuestion ->
                                examQuestion.getQuestion().getQuestionId()
                        )
                        .toList();


        List<Long> availableQuestionIds =
                availableQuestions.stream()
                        .map(Question::getQuestionId)
                        .filter(questionId -> !addedQuestionIds.contains(questionId))
                        .toList();


        return questionService.getQuestionsByIds(availableQuestionIds);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getExamQuestions(Long examId) {

        if (!examRepository.existsById(examId)) {
            throw new RuntimeException(
                    "Exam not found with id: " + examId
            );
        }

        List<ExamQuestion> examQuestions =
                examQuestionRepository
                        .findByExam_ExamId(examId);

        List<Long> questionIds =
                examQuestions.stream()
                        .map(examQuestion ->
                                examQuestion
                                        .getQuestion()
                                        .getQuestionId()
                        )
                        .toList();

        return questionService.getQuestionsByIds(questionIds);
    }


    @Transactional
    public void removeQuestionFromExam(
            Long examId,
            Long questionId) {

        if (!examRepository.existsById(examId)) {
            throw new RuntimeException(
                    "Exam not found with id: " + examId
            );
        }

        boolean exists =
                examQuestionRepository
                        .existsByExam_ExamIdAndQuestion_QuestionId(
                                examId,
                                questionId
                        );

        if (!exists) {
            throw new RuntimeException(
                    "Question is not added to this exam"
            );
        }

        examQuestionRepository
                .deleteByExam_ExamIdAndQuestion_QuestionId(
                        examId,
                        questionId
                );
    }


    private ExamResponseDTO convertToResponse(
            Exam exam) {

        ExamResponseDTO response =
                new ExamResponseDTO();


        response.setExamId(
                exam.getExamId()
        );


        response.setExamName(
                exam.getExamName()
        );


        response.setCollegeId(
                exam.getCollege()
                        .getCollegeId()
        );


        response.setBranchId(
                exam.getBranch()
                        .getBranchId()
        );

        response.setCourseId(
                exam.getCourse()
                        .getCourseId()
        );

        response.setCourseName(
                exam.getCourse()
                        .getName()
        );

        response.setSectionId(
                exam.getSection()
                        .getSectionId()
        );


        response.setChapterIds(
                exam.getChapters()
                        .stream()
                        .map(Chapter::getChapterId)
                        .toList()
        );


        response.setStartDate(
                exam.getStartDate()
        );


        response.setEndDate(
                exam.getEndDate()
        );

        response.setPassPercentage(
                exam.getPassPercentage()
        );


        response.setActiveRow(
                exam.getActiveRow()
        );


        response.setRowStatus(
                exam.getRowStatus()
        );


        response.setCreatedAt(
                exam.getCreatedAt()
        );


        response.setUpdatedAt(
                exam.getUpdatedAt()
        );


        return response;
    }
}
