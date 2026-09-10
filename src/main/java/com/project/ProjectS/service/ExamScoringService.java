package com.project.ProjectS.service;

import com.project.ProjectS.entity.McqOption;
import com.project.ProjectS.entity.QuestionAttribute;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.model.ExamAnswerDTO;
import com.project.ProjectS.model.ExamQuestionAnswerDTO;
import com.project.ProjectS.model.RuleConditionDTO;
import com.project.ProjectS.model.RuleEngineResponse;
import com.project.ProjectS.repository.McqOptionRepository;
import com.project.ProjectS.repository.QuestionAttributeRepository;
import com.project.ProjectS.repository.TableHeaderRepository;
import com.project.ProjectS.repository.TableNameRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scores a set of answered questions against the stored correct answers.
 *
 * This logic was extracted verbatim from {@code ExamService.submitExam} so that
 * real exams and mock exams score submissions in exactly the same way. Both
 * {@link ExamService} and {@code MockExamService} delegate here.
 */
@Service
public class ExamScoringService {

    /** Marks awarded and the maximum that could have been awarded. */
    public record Score(double totalMarks, double maximumMarks) {

        public double percentage() {
            return maximumMarks == 0
                    ? 0
                    : (totalMarks / maximumMarks) * 100;
        }
    }

    private final QuestionAttributeRepository questionAttributeRepository;
    private final McqOptionRepository mcqOptionRepository;
    private final RuleEngineService ruleEngineService;
    private final TableNameRepository tableNameRepository;
    private final TableHeaderRepository tableHeaderRepository;

    public ExamScoringService(
            QuestionAttributeRepository questionAttributeRepository,
            McqOptionRepository mcqOptionRepository,
            RuleEngineService ruleEngineService,
            TableNameRepository tableNameRepository,
            TableHeaderRepository tableHeaderRepository) {

        this.questionAttributeRepository = questionAttributeRepository;
        this.mcqOptionRepository = mcqOptionRepository;
        this.ruleEngineService = ruleEngineService;
        this.tableNameRepository = tableNameRepository;
        this.tableHeaderRepository = tableHeaderRepository;
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

        return new Score(totalMarks, maximumMarks);
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
}
