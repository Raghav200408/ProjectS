package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionAttribute;
import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;

import com.project.ProjectS.mapper.QuestionExcelMapper;
import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;

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
            Integer categoryId,
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

        if (categoryId == null) {
            throw new RuntimeException(
                    "Category ID is required"
            );
        }

        System.out.println("======================================");
        System.out.println("PROCESSING QUESTION EXCEL");
        System.out.println("Course ID   = " + courseId);
        System.out.println("Chapter ID  = " + chapterId);
        System.out.println("Category ID = " + categoryId);
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

                response.getMessages().add(
                        "Question upload failed: " +
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
                    categoryId
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
            Integer categoryId) {

        if (questionRows == null ||
                questionRows.isEmpty()) {

            return;
        }

        // =====================================================
        // VALIDATION PHASE
        // =====================================================

        List<ValidatedAttribute> validatedAttributes =
                new ArrayList<>();

        boolean questionValid = true;

        String failureMessage = null;

        try {

            // =================================================
            // STEP 1
            // CREATE QUESTION OBJECT
            //
            // NOTHING SAVED YET
            // =================================================

            Question question =
                    questionExcelMapper.map(
                            questionRows.get(0),
                            courseId,
                            chapterId,
                            categoryId
                    );

            System.out.println("--------------------------------------");
            System.out.println(
                    "Validating question: "
                            + question.getQuestionText()
            );

            // =================================================
            // STEP 2
            // VALIDATE EVERY ATTRIBUTE
            // =================================================

            for (Map<String, String> row
                    : questionRows) {

                // =================================================
                // HEADER
                // =================================================

                String headerName =
                        row.get("header_name");

                if (isBlank(headerName)) {

                    throw new RuntimeException(
                            "Header name is required"
                    );
                }

                TableHeader header =
                        tableHeaderRepository
                                .findByName(
                                        headerName.trim()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Table header not found: "
                                                        + headerName
                                        )
                                );

                System.out.println(
                        "Header found: "
                                + header.getName()
                                + " | ID = "
                                + header.getHeaderId()
                );

                // =================================================
                // ATTRIBUTE
                // =================================================

                String attributeName =
                        row.get("attribute_name");

                if (isBlank(attributeName)) {

                    throw new RuntimeException(
                            "Attribute name is required"
                    );
                }

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

                    throw new QuestionRejectedException(

                            "Rule not defined for this attribute: "
                                    + attributeName
                                    + ". Attribute was created as DRAFT. "
                                    + "Define the rule and upload again."
                    );
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

                    throw new QuestionRejectedException(

                            "Rule not defined for this attribute: "
                                    + attributeName
                                    + ". Define the rule and upload again."
                    );
                }

                // =================================================
                // ATTRIBUTE IS RULE
                // STORE FOR LATER
                // =================================================

                ValidatedAttribute validated =
                        new ValidatedAttribute();

                validated.setRow(row);

                validated.setHeader(header);

                validated.setAttribute(attribute);

                validatedAttributes.add(
                        validated
                );
            }

            // =====================================================
            // ALL ATTRIBUTES ARE VALID
            // NOW SAVE QUESTION
            // =====================================================

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

            // =====================================================
            // SAVE QUESTION ATTRIBUTES
            // =====================================================

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

                    try {

                        questionAttribute.setTransactionDate(
                                LocalDate.parse(
                                        transactionDate.trim()
                                )
                        );

                    } catch (Exception e) {

                        throw new RuntimeException(
                                "Invalid transaction_date: "
                                        + transactionDate
                        );
                    }
                }

                // =================================================
                // AMOUNT
                // =================================================

                String amount =
                        row.get("amount");

                if (!isBlank(amount)
                        && !"null".equalsIgnoreCase(
                        amount.trim())) {

                    try {

                        questionAttribute.setAmount(
                                new BigDecimal(
                                        amount.trim()
                                )
                        );

                    } catch (Exception e) {

                        throw new RuntimeException(
                                "Invalid amount: "
                                        + amount
                        );
                    }
                }

                // =================================================
                // AMOUNT 2
                // =================================================

                String amount2 =
                        row.get("amount2");

                if (!isBlank(amount2)
                        && !"null".equalsIgnoreCase(
                        amount2.trim())) {

                    try {

                        questionAttribute.setAmount2(
                                new BigDecimal(
                                        amount2.trim()
                                )
                        );

                    } catch (Exception e) {

                        throw new RuntimeException(
                                "Invalid amount2: "
                                        + amount2
                        );
                    }
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
                // SAVE QUESTION ATTRIBUTE
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

            // =====================================================
            // VERIFY ATTRIBUTE COUNT
            // =====================================================

            System.out.println(
                    "TOTAL QUESTION ATTRIBUTES SAVED = "
                            + savedAttributeCount
            );

            // =====================================================
            // SUCCESS
            // =====================================================

            response.setUploadedQuestions(
                    response.getUploadedQuestions() + 1
            );

            System.out.println(
                    "QUESTION UPLOAD SUCCESSFUL:"
                            + " Question ID = "
                            + savedQuestion.getQuestionId()
            );

            System.out.println("--------------------------------------");

        } catch (QuestionRejectedException e) {

            // =====================================================
            // QUESTION REJECTED
            // =====================================================

            questionValid = false;

            failureMessage =
                    e.getMessage();

        } catch (Exception e) {

            // =====================================================
            // OTHER ERROR
            // =====================================================

            questionValid = false;

            failureMessage =
                    "Question upload failed: "
                            + e.getMessage();

            e.printStackTrace();
        }

        // =====================================================
        // HANDLE REJECTED QUESTION
        // =====================================================

        if (!questionValid) {

            response.setSkippedRows(
                    response.getSkippedRows()
                            + questionRows.size()
            );

            response.getMessages().add(
                    "Question skipped: "
                            + failureMessage
            );

            System.out.println(
                    "QUESTION SKIPPED: "
                            + failureMessage
            );
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

    // =========================================================
    // QUESTION REJECTED EXCEPTION
    // =========================================================

    private static class QuestionRejectedException
            extends RuntimeException {

        public QuestionRejectedException(
                String message) {

            super(message);
        }
    }
}