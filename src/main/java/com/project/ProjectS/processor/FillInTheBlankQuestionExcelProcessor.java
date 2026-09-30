package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionFillBlankAnswer;

import com.project.ProjectS.mapper.FillInTheBlankQuestionExcelMapper;

import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import com.project.ProjectS.model.QuestionUploadErrorDTO;

import com.project.ProjectS.repository.QuestionFillBlankAnswerRepository;
import com.project.ProjectS.repository.QuestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FillInTheBlankQuestionExcelProcessor {

    private final FillInTheBlankQuestionExcelMapper mapper;

    private final QuestionRepository questionRepository;

    private final QuestionFillBlankAnswerRepository
            questionFillBlankAnswerRepository;

    @Autowired
    public FillInTheBlankQuestionExcelProcessor(
            FillInTheBlankQuestionExcelMapper mapper,
            QuestionRepository questionRepository,
            QuestionFillBlankAnswerRepository
                    questionFillBlankAnswerRepository) {

        this.mapper = mapper;

        this.questionRepository = questionRepository;

        this.questionFillBlankAnswerRepository =
                questionFillBlankAnswerRepository;
    }

    // =========================================================
    // PROCESS EXCEL
    // =========================================================

    @Transactional
    public QuestionExcelUploadResponseDTO process(
            List<Map<String, String>> excelData,
            Integer topicId,
            Integer chapterId,
            Integer courseId) {

        QuestionExcelUploadResponseDTO response =
                new QuestionExcelUploadResponseDTO();

        if (excelData == null) {

            throw new RuntimeException(
                    "Excel data is null"
            );
        }

        response.setTotalRows(
                excelData.size()
        );

        // =====================================================
        // VALIDATE IDS
        // =====================================================

        if (courseId == null) {

            throw new RuntimeException(
                    "Course ID is required"
            );
        }

        if (chapterId == null) {

            throw new RuntimeException(
                    "Chapter ID is required"
            );
        }

        if (topicId == null) {

            throw new RuntimeException(
                    "Topic ID is required"
            );
        }

        System.out.println(
                "======================================"
        );

        System.out.println(
                "PROCESSING FILL-IN-THE-BLANKS EXCEL"
        );

        System.out.println(
                "Course ID  = " + courseId
        );

        System.out.println(
                "Chapter ID = " + chapterId
        );

        System.out.println(
                "Topic ID   = " + topicId
        );

        System.out.println(
                "Total Rows = " + excelData.size()
        );

        System.out.println(
                "======================================"
        );

        // =====================================================
        // GROUP ROWS BY QUESTION TEXT
        // =====================================================

        Map<String, List<Map<String, String>>>
                questionRowsMap =
                new LinkedHashMap<>();

        for (Map<String, String> row : excelData) {

            // =================================================
            // IGNORE EMPTY ROW
            // =================================================

            if (row == null ||
                    row.values()
                            .stream()
                            .allMatch(value ->
                                    value == null ||
                                            value.isBlank())) {

                continue;
            }

            // =================================================
            // QUESTION TEXT
            // =================================================

            String questionText =
                    row.get("question_text");

            if (isBlank(questionText)) {

                response.setSkippedRows(
                        response.getSkippedRows() + 1
                );

                addError(
                        response.getErrors(),
                        getRowNumber(row),
                        null,
                        row.get("answer_text"),
                        "Question text is required"
                );

                continue;
            }

            // =================================================
            // GROUP BY QUESTION TEXT
            // =================================================

            String questionKey =
                    questionText.trim();

            questionRowsMap
                    .computeIfAbsent(
                            questionKey,
                            key -> new ArrayList<>()
                    )
                    .add(row);
        }

        // =====================================================
        // PROCESS EACH QUESTION
        // =====================================================

        System.out.println(
                "Number of questions found = "
                        + questionRowsMap.size()
        );

        for (List<Map<String, String>> questionRows
                : questionRowsMap.values()) {

            processCompleteQuestion(
                    questionRows,
                    response,
                    courseId,
                    chapterId,
                    topicId
            );
        }

        // =====================================================
        // FINAL RESPONSE
        // =====================================================

        if (response.getFailedQuestions() == 0) {

            response.setSuccess(true);

            response.setMessage(
                    "All Fill-in-the-Blanks questions uploaded successfully"
            );

        } else {

            response.setSuccess(false);

            response.setMessage(
                    "Upload completed with validation errors"
            );
        }

        return response;
    }

    // =========================================================
    // PROCESS COMPLETE QUESTION
    // =========================================================

    private void processCompleteQuestion(
            List<Map<String, String>> questionRows,
            QuestionExcelUploadResponseDTO response,
            Integer courseId,
            Integer chapterId,
            Integer topicId) {

        if (questionRows == null ||
                questionRows.isEmpty()) {

            return;
        }

        // =====================================================
        // VALIDATION
        // =====================================================

        List<ValidatedAnswer> validatedAnswers =
                new ArrayList<>();

        List<QuestionUploadErrorDTO> questionErrors =
                new ArrayList<>();

        Question question;

        // =====================================================
        // MAP QUESTION
        // =====================================================

        try {

            question =
                    mapper.map(
                            questionRows.get(0),
                            courseId,
                            chapterId,
                            topicId
                    );

        } catch (Exception e) {

            addQuestionLevelError(
                    questionErrors,
                    questionRows,
                    e.getMessage()
            );

            handleFailedQuestion(
                    questionRows,
                    response,
                    questionErrors
            );

            return;
        }

        System.out.println(
                "--------------------------------------"
        );

        System.out.println(
                "Validating question: "
                        + question.getQuestionText()
        );

        // =====================================================
        // VALIDATE EVERY ANSWER ROW
        // =====================================================

        for (Map<String, String> row
                : questionRows) {

            int rowNumber =
                    getRowNumber(row);

            String answerText =
                    row.get("answer_text");

            String blankNumberText =
                    row.get("blank_number");

            String isCorrectText =
                    row.get("is_correct");

            String displayOrderText =
                    row.get("display_order");

            // =================================================
            // ANSWER TEXT REQUIRED
            // =================================================

            if (isBlank(answerText)) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        answerText,
                        "Answer text is required"
                );

                continue;
            }

            // =================================================
            // BLANK NUMBER REQUIRED
            // =================================================

            if (isBlank(blankNumberText)) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        answerText,
                        "Blank number is required"
                );

                continue;
            }

            Integer blankNumber;

            try {

                blankNumber =
                        Integer.parseInt(
                                blankNumberText.trim()
                        );

                if (blankNumber <= 0) {

                    throw new NumberFormatException();
                }

            } catch (Exception e) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        answerText,
                        "Invalid blank_number: "
                                + blankNumberText
                );

                continue;
            }

            // =================================================
            // IS CORRECT REQUIRED
            // =================================================

            if (isBlank(isCorrectText)) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        answerText,
                        "is_correct is required"
                );

                continue;
            }

            Boolean isCorrect;

            try {

                isCorrect =
                        parseBoolean(
                                isCorrectText
                        );

            } catch (Exception e) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        answerText,
                        "Invalid is_correct value: "
                                + isCorrectText
                                + ". Use true/false."
                );

                continue;
            }

            // =================================================
            // DISPLAY ORDER REQUIRED
            // =================================================

            if (isBlank(displayOrderText)) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        answerText,
                        "Display order is required"
                );

                continue;
            }

            Integer displayOrder;

            try {

                displayOrder =
                        Integer.parseInt(
                                displayOrderText.trim()
                        );

                if (displayOrder <= 0) {

                    throw new NumberFormatException();
                }

            } catch (Exception e) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        answerText,
                        "Invalid display_order: "
                                + displayOrderText
                );

                continue;
            }

            // =================================================
            // STORE VALIDATED ANSWER
            // =================================================

            ValidatedAnswer validatedAnswer =
                    new ValidatedAnswer();

            validatedAnswer.setAnswerText(
                    answerText.trim()
            );

            validatedAnswer.setBlankNumber(
                    blankNumber
            );

            validatedAnswer.setIsCorrect(
                    isCorrect
            );

            validatedAnswer.setDisplayOrder(
                    displayOrder
            );

            validatedAnswers.add(
                    validatedAnswer
            );
        }

        // =====================================================
        // AT LEAST ONE ANSWER REQUIRED
        // =====================================================

        if (validatedAnswers.isEmpty()
                && questionErrors.isEmpty()) {

            addQuestionLevelError(
                    questionErrors,
                    questionRows,
                    "At least one answer is required"
            );
        }

        // =====================================================
        // VALIDATE DUPLICATE DISPLAY ORDER
        // =====================================================

        if (questionErrors.isEmpty()) {

            java.util.Set<Integer> displayOrders =
                    new java.util.HashSet<>();

            for (ValidatedAnswer answer
                    : validatedAnswers) {

                if (!displayOrders.add(
                        answer.getDisplayOrder())) {

                    addQuestionLevelError(
                            questionErrors,
                            questionRows,
                            "Duplicate display_order found: "
                                    + answer.getDisplayOrder()
                    );

                    break;
                }
            }
        }

        // =====================================================
        // VALIDATE BLANK NUMBERS
        // =====================================================

        if (questionErrors.isEmpty()) {

            java.util.Set<Integer> blankNumbers =
                    new java.util.HashSet<>();

            for (ValidatedAnswer answer
                    : validatedAnswers) {

                blankNumbers.add(
                        answer.getBlankNumber()
                );
            }

            for (Integer blankNumber
                    : blankNumbers) {

                boolean hasCorrectAnswer =
                        validatedAnswers
                                .stream()
                                .anyMatch(answer ->
                                        answer.getBlankNumber()
                                                .equals(blankNumber)
                                                &&
                                                Boolean.TRUE.equals(
                                                        answer.getIsCorrect()
                                                )
                                );

                if (!hasCorrectAnswer) {

                    addQuestionLevelError(
                            questionErrors,
                            questionRows,
                            "Blank number "
                                    + blankNumber
                                    + " must have at least one correct answer"
                    );

                    break;
                }
            }
        }

        // =====================================================
        // QUESTION HAS ERRORS
        // =====================================================

        if (!questionErrors.isEmpty()) {

            handleFailedQuestion(
                    questionRows,
                    response,
                    questionErrors
            );

            return;
        }

        // =====================================================
        // SAVE QUESTION
        // =====================================================

        try {

            Question savedQuestion =
                    questionRepository.save(
                            question
                    );

            questionRepository.flush();

            System.out.println(
                    "QUESTION SAVED:"
                            + " ID = "
                            + savedQuestion.getQuestionId()
                            + " | Text = "
                            + savedQuestion.getQuestionText()
            );

            // =================================================
            // SAVE ANSWERS
            // =================================================

            for (ValidatedAnswer validatedAnswer
                    : validatedAnswers) {

                QuestionFillBlankAnswer answer =
                        new QuestionFillBlankAnswer();

                answer.setQuestion(
                        savedQuestion
                );

                answer.setAnswerText(
                        validatedAnswer.getAnswerText()
                );

                answer.setDisplayOrder(
                        validatedAnswer.getDisplayOrder()
                );

                answer.setIsCorrect(
                        validatedAnswer.getIsCorrect()
                );

                answer.setBlankNumber(
                        validatedAnswer.getBlankNumber()
                );

                QuestionFillBlankAnswer savedAnswer =
                        questionFillBlankAnswerRepository.save(
                                answer
                        );

                System.out.println(
                        "ANSWER SAVED:"
                                + " ID = "
                                + savedAnswer.getAnswerId()
                                + " | Blank = "
                                + savedAnswer.getBlankNumber()
                                + " | Answer = "
                                + savedAnswer.getAnswerText()
                                + " | Correct = "
                                + savedAnswer.getIsCorrect()
                );
            }

            questionFillBlankAnswerRepository.flush();

            // =================================================
            // SUCCESS
            // =================================================

            response.setUploadedQuestions(
                    response.getUploadedQuestions() + 1
            );

            System.out.println(
                    "FILL-IN-THE-BLANK QUESTION UPLOAD SUCCESSFUL:"
                            + " Question ID = "
                            + savedQuestion.getQuestionId()
            );

            System.out.println(
                    "--------------------------------------"
            );

        } catch (Exception e) {

            List<QuestionUploadErrorDTO> saveErrors =
                    new ArrayList<>();

            addQuestionLevelError(
                    saveErrors,
                    questionRows,
                    "Question could not be saved: "
                            + e.getMessage()
            );

            handleFailedQuestion(
                    questionRows,
                    response,
                    saveErrors
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // PARSE BOOLEAN
    // =========================================================

    private Boolean parseBoolean(String value) {

        if (value == null) {

            throw new RuntimeException(
                    "Boolean value is null"
            );
        }

        String normalized =
                value.trim().toLowerCase();

        if ("true".equals(normalized)
                || "yes".equals(normalized)
                || "1".equals(normalized)) {

            return true;
        }

        if ("false".equals(normalized)
                || "no".equals(normalized)
                || "0".equals(normalized)) {

            return false;
        }

        throw new RuntimeException(
                "Invalid boolean value"
        );
    }

    // =========================================================
    // HANDLE FAILED QUESTION
    // =========================================================

    private void handleFailedQuestion(
            List<Map<String, String>> questionRows,
            QuestionExcelUploadResponseDTO response,
            List<QuestionUploadErrorDTO> errors) {

        response.setFailedQuestions(
                response.getFailedQuestions() + 1
        );

        response.setSkippedRows(
                response.getSkippedRows()
                        + questionRows.size()
        );

        response.getErrors().addAll(
                errors
        );

        System.out.println(
                "FILL-IN-THE-BLANK QUESTION SKIPPED"
        );

        for (QuestionUploadErrorDTO error
                : errors) {

            System.out.println(
                    "Row "
                            + error.getRowNumber()
                            + " | Answer = "
                            + error.getAttributeName()
                            + " | Error = "
                            + error.getErrorMessage()
            );
        }
    }

    // =========================================================
    // ADD ERROR
    // =========================================================

    private void addError(
            List<QuestionUploadErrorDTO> errors,
            int rowNumber,
            String questionText,
            String answerText,
            String errorMessage) {

        QuestionUploadErrorDTO error =
                new QuestionUploadErrorDTO();

        error.setRowNumber(
                rowNumber
        );

        error.setQuestionText(
                questionText
        );

        error.setHeaderName(
                null
        );

        error.setAttributeName(
                answerText
        );

        error.setErrorMessage(
                errorMessage
        );

        errors.add(
                error
        );
    }

    // =========================================================
    // ADD QUESTION LEVEL ERROR
    // =========================================================

    private void addQuestionLevelError(
            List<QuestionUploadErrorDTO> errors,
            List<Map<String, String>> questionRows,
            String errorMessage) {

        String questionText =
                questionRows.isEmpty()
                        ? null
                        : questionRows
                        .get(0)
                        .get("question_text");

        int rowNumber =
                questionRows.isEmpty()
                        ? 0
                        : getRowNumber(
                        questionRows.get(0)
                );

        addError(
                errors,
                rowNumber,
                questionText,
                null,
                errorMessage
        );
    }

    // =========================================================
    // GET EXCEL ROW NUMBER
    // =========================================================

    private int getRowNumber(
            Map<String, String> row) {

        if (row == null) {

            return 0;
        }

        String rowNumber =
                row.get("_excel_row_number");

        if (isBlank(rowNumber)) {

            return 0;
        }

        try {

            return Integer.parseInt(
                    rowNumber
            );

        } catch (NumberFormatException e) {

            return 0;
        }
    }

    // =========================================================
    // IS BLANK
    // =========================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    // =========================================================
    // VALIDATED ANSWER
    // =========================================================

    private static class ValidatedAnswer {

        private String answerText;

        private Integer displayOrder;

        private Boolean isCorrect;

        private Integer blankNumber;

        public String getAnswerText() {

            return answerText;
        }

        public void setAnswerText(
                String answerText) {

            this.answerText = answerText;
        }

        public Integer getDisplayOrder() {

            return displayOrder;
        }

        public void setDisplayOrder(
                Integer displayOrder) {

            this.displayOrder = displayOrder;
        }

        public Boolean getIsCorrect() {

            return isCorrect;
        }

        public void setIsCorrect(
                Boolean isCorrect) {

            this.isCorrect = isCorrect;
        }

        public Integer getBlankNumber() {

            return blankNumber;
        }

        public void setBlankNumber(
                Integer blankNumber) {

            this.blankNumber = blankNumber;
        }
    }
}