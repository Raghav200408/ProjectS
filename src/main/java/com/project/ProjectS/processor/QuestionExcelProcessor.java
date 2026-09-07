package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionAttribute;
import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;

import com.project.ProjectS.mapper.QuestionExcelMapper;
import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import com.project.ProjectS.model.QuestionUploadErrorDTO;

import com.project.ProjectS.repository.QuestionAttributeRepository;
import com.project.ProjectS.repository.QuestionRepository;
import com.project.ProjectS.repository.TableAttributeRepository;
import com.project.ProjectS.repository.TableHeaderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionExcelProcessor {

    private final QuestionExcelMapper questionExcelMapper;

    private final QuestionRepository questionRepository;

    private final QuestionAttributeRepository questionAttributeRepository;

    private final TableHeaderRepository tableHeaderRepository;

    private final TableAttributeRepository tableAttributeRepository;

    @Autowired
    public QuestionExcelProcessor(
            QuestionExcelMapper questionExcelMapper,
            QuestionRepository questionRepository,
            QuestionAttributeRepository questionAttributeRepository,
            TableHeaderRepository tableHeaderRepository,
            TableAttributeRepository tableAttributeRepository) {

        this.questionExcelMapper =
                questionExcelMapper;

        this.questionRepository =
                questionRepository;

        this.questionAttributeRepository =
                questionAttributeRepository;

        this.tableHeaderRepository =
                tableHeaderRepository;

        this.tableAttributeRepository =
                tableAttributeRepository;
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

        System.out.println("======================================");
        System.out.println("PROCESSING QUESTION EXCEL");
        System.out.println("Course ID   = " + courseId);
        System.out.println("Chapter ID  = " + chapterId);
        System.out.println("Topic ID = " + topicId);
        System.out.println("Total Rows  = " + excelData.size());
        System.out.println("======================================");

        // =====================================================
        // GROUP EXCEL ROWS BY QUESTION
        // =====================================================

        Map<String, List<Map<String, String>>> questionRowsMap =
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
                        row.get("header_name"),
                        row.get("attribute_name"),
                        "Question text is required"
                );

                continue;
            }

            // =================================================
            // GROUP BY QUESTION TEXT
            // =================================================

            String questionKey =
                    buildQuestionKey(row);

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
                    "All questions uploaded successfully"
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
        // VALIDATION PHASE
        // =====================================================

        List<ValidatedAttribute> validatedAttributes =
                new ArrayList<>();

        List<QuestionUploadErrorDTO> questionErrors =
                new ArrayList<>();

        Question question;

        // =====================================================
        // CREATE QUESTION OBJECT
        // =====================================================

        try {

            question =
                    questionExcelMapper.map(
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

        System.out.println("--------------------------------------");

        System.out.println(
                "Validating question: "
                        + question.getQuestionText()
        );

        // =====================================================
        // VALIDATE EVERY ATTRIBUTE
        // =====================================================

        for (Map<String, String> row
                : questionRows) {

            String headerName =
                    row.get("header_name");

            String attributeName =
                    row.get("attribute_name");

            int rowNumber =
                    getRowNumber(row);

            // =================================================
            // HEADER REQUIRED
            // =================================================

            if (isBlank(headerName)) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        headerName,
                        attributeName,
                        "Header name is required"
                );

                continue;
            }

            // =================================================
            // FIND HEADER
            // =================================================

            TableHeader header =
                    tableHeaderRepository
                            .findByName(
                                    headerName.trim()
                            )
                            .orElse(null);

            if (header == null) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        headerName,
                        attributeName,
                        "Table header not found: "
                                + headerName
                );

                continue;
            }

            System.out.println(
                    "Header found: "
                            + header.getName()
                            + " | ID = "
                            + header.getHeaderId()
            );

            // =================================================
            // ATTRIBUTE REQUIRED
            // =================================================

            if (isBlank(attributeName)) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        headerName,
                        attributeName,
                        "Attribute name is required"
                );

                continue;
            }

            // =================================================
            // FIND ATTRIBUTE
            // =================================================

            TableAttribute attribute =
                    tableAttributeRepository
                            .findByNameAndTableHeader(
                                    attributeName.trim(),
                                    header
                            )
                            .orElse(null);

            // =================================================
            // ATTRIBUTE NOT FOUND
            // =================================================

            if (attribute == null) {

                System.out.println(
                        "Attribute NOT FOUND: "
                                + attributeName
                );

                TableAttribute newAttribute =
                        new TableAttribute();

                newAttribute.setName(
                        attributeName.trim()
                );

                newAttribute.setTableHeader(
                        header
                );

                newAttribute.setRowStatus(
                        "DRAFT"
                );

                newAttribute.setActiveRow(
                        true
                );

                newAttribute.setRowDisable(
                        false
                );

                TableAttribute savedAttribute =
                        tableAttributeRepository.save(
                                newAttribute
                        );

                tableAttributeRepository.flush();

                System.out.println(
                        "NEW ATTRIBUTE CREATED:"
                                + " ID = "
                                + savedAttribute.getAttributeId()
                                + " | Name = "
                                + savedAttribute.getName()
                                + " | Status = DRAFT"
                );

                response.setCreatedAttributes(
                        response.getCreatedAttributes() + 1
                );

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        headerName,
                        attributeName,
                        "Rule not defined for this attribute. "
                                + "Attribute was created as DRAFT. "
                                + "Define the rule and upload again."
                );

                // IMPORTANT:
                // Do NOT stop processing this question.
                continue;
            }

            // =================================================
            // ATTRIBUTE FOUND
            // =================================================

            System.out.println(
                    "Attribute found: "
                            + attribute.getName()
                            + " | ID = "
                            + attribute.getAttributeId()
                            + " | Status = "
                            + attribute.getRowStatus()
            );

            // =================================================
            // CHECK ATTRIBUTE STATUS
            // =================================================

            if (!"RULE".equalsIgnoreCase(
                    attribute.getRowStatus())) {

                addError(
                        questionErrors,
                        rowNumber,
                        question.getQuestionText(),
                        headerName,
                        attributeName,
                        "Rule not defined for this attribute. "
                                + "Define the rule and upload again."
                );

                // IMPORTANT:
                // Continue checking remaining attributes.
                continue;
            }

            // =================================================
            // VALIDATE DATA VALUES
            // =================================================

            boolean dataValid = true;

            String transactionDate =
                    row.get("transaction_date");

            if (!isBlank(transactionDate)
                    && !"null".equalsIgnoreCase(
                    transactionDate.trim())) {

                try {

                    LocalDate.parse(
                            transactionDate.trim()
                    );

                } catch (Exception e) {

                    dataValid = false;

                    addError(
                            questionErrors,
                            rowNumber,
                            question.getQuestionText(),
                            headerName,
                            attributeName,
                            "Invalid transaction_date: "
                                    + transactionDate
                    );
                }
            }

            String amount =
                    row.get("amount");

            if (!isBlank(amount)
                    && !"null".equalsIgnoreCase(
                    amount.trim())) {

                try {

                    new BigDecimal(
                            amount.trim()
                    );

                } catch (Exception e) {

                    dataValid = false;

                    addError(
                            questionErrors,
                            rowNumber,
                            question.getQuestionText(),
                            headerName,
                            attributeName,
                            "Invalid amount: "
                                    + amount
                    );
                }
            }

            String amount2 =
                    row.get("amount2");

            if (!isBlank(amount2)
                    && !"null".equalsIgnoreCase(
                    amount2.trim())) {

                try {

                    new BigDecimal(
                            amount2.trim()
                    );

                } catch (Exception e) {

                    dataValid = false;

                    addError(
                            questionErrors,
                            rowNumber,
                            question.getQuestionText(),
                            headerName,
                            attributeName,
                            "Invalid amount2: "
                                    + amount2
                    );
                }
            }

            // =================================================
            // STORE ONLY FULLY VALID ATTRIBUTE
            // =================================================

            if (dataValid) {

                ValidatedAttribute validated =
                        new ValidatedAttribute();

                validated.setRow(row);

                validated.setHeader(header);

                validated.setAttribute(attribute);

                validatedAttributes.add(
                        validated
                );
            }
        }

        // =====================================================
        // QUESTION HAS ERRORS
        // DON'T SAVE QUESTION
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
        // ALL ATTRIBUTES VALID
        // NOW SAVE QUESTION
        // =====================================================

        try {

            System.out.println(
                    "VALIDATED ATTRIBUTES COUNT = "
                            + validatedAttributes.size()
            );

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
            // SAVE QUESTION ATTRIBUTES
            // =================================================

            int savedAttributeCount = 0;

            for (ValidatedAttribute validated
                    : validatedAttributes) {

                QuestionAttribute questionAttribute =
                        new QuestionAttribute();

                // =================================================
                // QUESTION
                // =================================================

                questionAttribute.setQuestion(
                        savedQuestion
                );

                // =================================================
                // HEADER
                // =================================================

                questionAttribute.setHeader(
                        validated.getHeader()
                );

                // =================================================
                // ATTRIBUTE
                // =================================================

                questionAttribute.setAttribute(
                        validated.getAttribute()
                );

                Map<String, String> row =
                        validated.getRow();

                // =================================================
                // TRANSACTION DATE
                // =================================================

                String transactionDate =
                        row.get("transaction_date");

                if (!isBlank(transactionDate)
                        && !"null".equalsIgnoreCase(
                        transactionDate.trim())) {

                    questionAttribute.setTransactionDate(
                            LocalDate.parse(
                                    transactionDate.trim()
                            )
                    );
                }

                // =================================================
                // AMOUNT
                // =================================================

                String amount =
                        row.get("amount");

                if (!isBlank(amount)
                        && !"null".equalsIgnoreCase(
                        amount.trim())) {

                    questionAttribute.setAmount(
                            new BigDecimal(
                                    amount.trim()
                            )
                    );
                }

                // =================================================
                // AMOUNT 2
                // =================================================

                String amount2 =
                        row.get("amount2");

                if (!isBlank(amount2)
                        && !"null".equalsIgnoreCase(
                        amount2.trim())) {

                    questionAttribute.setAmount2(
                            new BigDecimal(
                                    amount2.trim()
                            )
                    );
                }

                // =================================================
                // NOTE
                // =================================================

                String note =
                        row.get("note");

                if (!isBlank(note)
                        && !"null".equalsIgnoreCase(
                        note.trim())) {

                    questionAttribute.setNote(
                            note.trim()
                    );
                }

                // =================================================
                // ACTIVE ROW
                // =================================================

                questionAttribute.setActiveRow(true);

                // =================================================
                // SAVE
                // =================================================

                QuestionAttribute savedQuestionAttribute =
                        questionAttributeRepository.save(
                                questionAttribute
                        );

                questionAttributeRepository.flush();

                savedAttributeCount++;

                System.out.println(
                        "QUESTION ATTRIBUTE SAVED:"
                );

                System.out.println(
                        "  Question Attribute ID = "
                                + savedQuestionAttribute
                                .getQuestionAttributeId()
                );

                System.out.println(
                        "  Question ID = "
                                + savedQuestion
                                .getQuestionId()
                );

                System.out.println(
                        "  Header ID = "
                                + validated
                                .getHeader()
                                .getHeaderId()
                );

                System.out.println(
                        "  Attribute ID = "
                                + validated
                                .getAttribute()
                                .getAttributeId()
                );

                System.out.println(
                        "  Attribute Name = "
                                + validated
                                .getAttribute()
                                .getName()
                );

                System.out.println(
                        "  Amount = "
                                + savedQuestionAttribute
                                .getAmount()
                );
            }

            // =================================================
            // SUCCESS
            // =================================================

            response.setUploadedQuestions(
                    response.getUploadedQuestions() + 1
            );

            System.out.println(
                    "QUESTION UPLOAD SUCCESSFUL:"
                            + " Question ID = "
                            + savedQuestion.getQuestionId()
            );

            System.out.println("--------------------------------------");

        } catch (Exception e) {

            // =================================================
            // DATABASE / SAVE ERROR
            // =================================================

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
                "QUESTION SKIPPED"
        );

        for (QuestionUploadErrorDTO error
                : errors) {

            System.out.println(
                    "  Row "
                            + error.getRowNumber()
                            + " | Attribute = "
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
            String headerName,
            String attributeName,
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
                headerName
        );

        error.setAttributeName(
                attributeName
        );

        error.setErrorMessage(
                errorMessage
        );

        errors.add(
                error
        );
    }

    // =========================================================
    // ADD ERROR TO QUESTION
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
    // BUILD QUESTION KEY
    // =========================================================

    private String buildQuestionKey(
            Map<String, String> row) {

        return safeValue(
                row.get("question_text")
        );
    }

    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String safeValue(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // =========================================================
    // IS BLANK
    // =========================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    // =========================================================
    // VALIDATED ATTRIBUTE
    // =========================================================

    private static class ValidatedAttribute {

        private Map<String, String> row;

        private TableHeader header;

        private TableAttribute attribute;

        public Map<String, String> getRow() {
            return row;
        }

        public void setRow(
                Map<String, String> row) {

            this.row = row;
        }

        public TableHeader getHeader() {
            return header;
        }

        public void setHeader(
                TableHeader header) {

            this.header = header;
        }

        public TableAttribute getAttribute() {
            return attribute;
        }

        public void setAttribute(
                TableAttribute attribute) {

            this.attribute = attribute;
        }
    }
}