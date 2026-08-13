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

    @Autowired
    private QuestionExcelMapper questionExcelMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionAttributeRepository questionAttributeRepository;

    @Autowired
    private TableHeaderRepository tableHeaderRepository;

    @Autowired
    private TableAttributeRepository tableAttributeRepository;


    // =========================================================
    // PROCESS EXCEL
    // =========================================================

    @Transactional
    public QuestionExcelUploadResponseDTO process(
            List<Map<String, String>> excelData) {

        QuestionExcelUploadResponseDTO response =
                new QuestionExcelUploadResponseDTO();

        response.setTotalRows(excelData.size());


        // =====================================================
        // GROUP EXCEL ROWS BY QUESTION
        // =====================================================

        Map<String, List<Map<String, String>>> questionRowsMap =
                new LinkedHashMap<>();


        for (Map<String, String> row : excelData) {

            if (row == null ||
                    row.values()
                            .stream()
                            .allMatch(value ->
                                    value == null || value.isBlank())) {

                continue;
            }

            String questionText =
                    row.get("question_text");

            if (isBlank(questionText)) {

                response.setSkippedRows(
                        response.getSkippedRows() + 1
                );

                response.getMessages().add(
                        "Question upload failed: Question text is required"
                );

                continue;
            }


            /*
             * Same question text + course + chapter + category
             * belongs to one question.
             */
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
        // PROCESS ONE COMPLETE QUESTION AT A TIME
        // =====================================================

        for (List<Map<String, String>> questionRows
                : questionRowsMap.values()) {

            processCompleteQuestion(
                    questionRows,
                    response
            );
        }


        return response;
    }


    // =========================================================
    // PROCESS COMPLETE QUESTION
    // =========================================================

    private void processCompleteQuestion(
            List<Map<String, String>> questionRows,
            QuestionExcelUploadResponseDTO response) {


        if (questionRows == null || questionRows.isEmpty()) {
            return;
        }


        // =====================================================
        // VALIDATION PHASE
        //
        // IMPORTANT:
        // NOTHING IS SAVED TO QUESTION TABLE HERE
        // =====================================================

        Question question = null;

        /*
         * Store validated attributes here.
         *
         * We only save these after ALL rows pass validation.
         */
        List<ValidatedAttribute> validatedAttributes =
                new ArrayList<>();


        boolean questionValid = true;

        String failureMessage = null;


        try {

            // =================================================
            // STEP 1
            // MAP FIRST ROW TO QUESTION
            //
            // Mapper finds:
            // Branch
            // Course
            // Chapter
            // Category
            // Question
            // =================================================

            question =
                    questionExcelMapper.map(
                            questionRows.get(0)
                    );


            // =================================================
            // STEP 2 - CHECK EVERY ROW
            // =================================================

            for (Map<String, String> row : questionRows) {


                // =================================================
                // FIND HEADER
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


                // =================================================
                // FIND ATTRIBUTE
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
                //
                // CREATE ATTRIBUTE AS DRAFT
                //
                // STOP ENTIRE QUESTION
                // =================================================

                if (attribute == null) {

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


                    tableAttributeRepository.save(
                            newAttribute
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
                // CHECK ROW STATUS
                // =================================================

                if (!"RULE".equalsIgnoreCase(
                        attribute.getRowStatus())) {


                    // =============================================
                    // ATTRIBUTE IS DRAFT
                    //
                    // STOP ENTIRE QUESTION
                    // =============================================

                    throw new QuestionRejectedException(
                            "Rule not defined for this attribute: "
                                    + attributeName
                                    + ". Define the rule and upload again."
                    );
                }


                // =================================================
                // ATTRIBUTE IS RULE
                // VALIDATE DATA
                // =================================================

                ValidatedAttribute validated =
                        new ValidatedAttribute();

                validated.setRow(row);
                validated.setHeader(header);
                validated.setAttribute(attribute);

                validatedAttributes.add(validated);
            }


            // =====================================================
            // ALL ATTRIBUTES ARE RULE
            //
            // NOW QUESTION CAN BE SAVED
            // =====================================================

            Question savedQuestion =
                    questionRepository.save(
                            question
                    );


            // =====================================================
            // SAVE ALL QUESTION ATTRIBUTES
            // =====================================================

            for (ValidatedAttribute validated
                    : validatedAttributes) {

                QuestionAttribute questionAttribute =
                        new QuestionAttribute();


                questionAttribute.setQuestion(
                        savedQuestion
                );


                questionAttribute.setHeader(
                        validated.getHeader()
                );


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


                if (!isBlank(transactionDate)) {

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


                if (!isBlank(amount)) {

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


                if (!isBlank(amount2)) {

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

                questionAttribute.setNote(
                        row.get("note")
                );


                // =================================================
                // SAVE QUESTION ATTRIBUTE
                // =================================================

                questionAttributeRepository.save(
                        questionAttribute
                );
            }


            // =====================================================
            // QUESTION SUCCESSFULLY UPLOADED
            // =====================================================

            response.setUploadedQuestions(
                    response.getUploadedQuestions() + 1
            );


        } catch (QuestionRejectedException e) {

            // =====================================================
            // ENTIRE QUESTION REJECTED
            //
            // IMPORTANT:
            // No Question was saved.
            // No QuestionAttribute was saved.
            // =====================================================

            questionValid = false;

            failureMessage = e.getMessage();


        } catch (Exception e) {

            // =====================================================
            // OTHER ERROR
            // =====================================================

            questionValid = false;

            failureMessage =
                    "Question upload failed: "
                            + e.getMessage();
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
        }
    }


    // =========================================================
    // BUILD QUESTION KEY
    // =========================================================

    private String buildQuestionKey(
            Map<String, String> row) {

        return safeValue(
                row.get("branch_name")
        )
                + "|"
                + safeValue(
                row.get("course_name")
        )
                + "|"
                + safeValue(
                row.get("chapter_name")
        )
                + "|"
                + safeValue(
                row.get("category_name")
        )
                + "|"
                + safeValue(
                row.get("question_text")
        )
                .trim();
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