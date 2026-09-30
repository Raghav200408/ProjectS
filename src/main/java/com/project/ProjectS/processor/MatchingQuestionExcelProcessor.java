package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionMatchingPair;
import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import com.project.ProjectS.model.QuestionUploadErrorDTO;
import com.project.ProjectS.repository.QuestionMatchingPairRepository;
import com.project.ProjectS.repository.QuestionRepository;
import com.project.ProjectS.mapper.MatchingQuestionExcelMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Component
public class MatchingQuestionExcelProcessor {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionMatchingPairRepository questionMatchingPairRepository;

    @Autowired
    private MatchingQuestionExcelMapper matchingQuestionExcelMapper;


    public QuestionExcelUploadResponseDTO processExcel(
            MultipartFile file,
            Integer courseId,
            Integer chapterId,
            Integer topicId) {

        QuestionExcelUploadResponseDTO response =
                new QuestionExcelUploadResponseDTO();

        List<QuestionUploadErrorDTO> errors =
                new ArrayList<>();

        int uploadedQuestions = 0;
        int skippedRows = 0;
        int failedQuestions = 0;
        int createdAttributes = 0;

        try {

            if (file == null || file.isEmpty()) {

                response.setSuccess(false);
                response.setMessage("Excel file is empty");
                return response;
            }


            /*
             * Read Excel
             */
            List<Map<String, String>> rows =
                    readExcel(file);

            response.setTotalRows(rows.size());

            if (rows.isEmpty()) {

                response.setSuccess(false);
                response.setMessage("Excel file contains no data");
                return response;
            }


            /*
             * Add Excel row number
             */
            int excelRowNumber = 2;

            for (Map<String, String> row : rows) {

                row.put(
                        "_excel_row_number",
                        String.valueOf(excelRowNumber)
                );

                excelRowNumber++;
            }


            /*
             * Group rows by question_text
             *
             * One question can contain
             * multiple matching pairs.
             */
            Map<String, List<Map<String, String>>> groupedQuestions =
                    new LinkedHashMap<>();

            for (Map<String, String> row : rows) {

                String questionText =
                        row.get("question_text");

                if (questionText == null ||
                        questionText.trim().isEmpty()) {

                    failedQuestions++;

                    errors.add(
                            createError(
                                    row.get("_excel_row_number"),
                                    "question_text is required"
                            )
                    );

                    continue;
                }

                questionText =
                        questionText.trim();

                groupedQuestions
                        .computeIfAbsent(
                                questionText,
                                key -> new ArrayList<>()
                        )
                        .add(row);
            }


            /*
             * Process each question
             */
            for (Map.Entry<String, List<Map<String, String>>> entry
                    : groupedQuestions.entrySet()) {

                String questionText =
                        entry.getKey();

                List<Map<String, String>> questionRows =
                        entry.getValue();

                try {

                    /*
                     * Validate all matching pairs
                     * before saving anything.
                     */
                    Set<Integer> displayOrders =
                            new HashSet<>();

                    for (Map<String, String> row :
                            questionRows) {

                        String columnA =
                                row.get("column_a");

                        String columnB =
                                row.get("column_b");

                        String displayOrderValue =
                                row.get("display_order");


                        if (columnA == null ||
                                columnA.trim().isEmpty()) {

                            throw new RuntimeException(
                                    "column_a is required"
                            );
                        }


                        if (columnB == null ||
                                columnB.trim().isEmpty()) {

                            throw new RuntimeException(
                                    "column_b is required"
                            );
                        }


                        if (displayOrderValue == null ||
                                displayOrderValue.trim().isEmpty()) {

                            throw new RuntimeException(
                                    "display_order is required"
                            );
                        }


                        int displayOrder;

                        try {

                            displayOrder =
                                    Integer.parseInt(
                                            displayOrderValue.trim()
                                    );

                        } catch (NumberFormatException e) {

                            throw new RuntimeException(
                                    "display_order must be a number"
                            );
                        }


                        if (displayOrder <= 0) {

                            throw new RuntimeException(
                                    "display_order must be greater than 0"
                            );
                        }


                        if (!displayOrders.add(displayOrder)) {

                            throw new RuntimeException(
                                    "Duplicate display_order found: "
                                            + displayOrder
                            );
                        }
                    }


                    /*
                     * Find existing MATCH_THE_FOLLOWING question
                     * for the same course, chapter and topic.
                     */
                    Optional<Question> existingQuestion =
                            questionRepository
                                    .findAll()
                                    .stream()
                                    .filter(question -> {

                                        if (question.getQuestionText() == null) {
                                            return false;
                                        }

                                        if (!question.getQuestionText()
                                                .trim()
                                                .equalsIgnoreCase(questionText)) {
                                            return false;
                                        }

                                        if (question.getCourse() == null ||
                                                question.getChapter() == null ||
                                                question.getTopic() == null ||
                                                question.getQuestionType() == null) {
                                            return false;
                                        }

                                        if (!Objects.equals(
                                                question.getCourse()
                                                        .getCourseId(),
                                                Long.valueOf(courseId))) {
                                            return false;
                                        }

                                        if (!Objects.equals(
                                                question.getChapter()
                                                        .getChapterId(),
                                                Long.valueOf(chapterId))) {
                                            return false;
                                        }

                                        if (!Objects.equals(
                                                question.getTopic()
                                                        .getTopicId(),
                                                Long.valueOf(topicId))) {
                                            return false;
                                        }

                                        return "MATCH_THE_FOLLOWING"
                                                .equalsIgnoreCase(
                                                        question
                                                                .getQuestionType()
                                                                .getQuestionType()
                                                );
                                    })
                                    .findFirst();


                    /*
                     * =====================================================
                     * CASE 1:
                     * Question does NOT exist
                     * =====================================================
                     */
                    if (existingQuestion.isEmpty()) {

                        /*
                         * Create Question entity
                         */
                        Map<String, String> firstRow =
                                questionRows.get(0);

                        Question question =
                                matchingQuestionExcelMapper
                                        .mapQuestion(
                                                firstRow,
                                                courseId,
                                                chapterId,
                                                topicId
                                        );


                        /*
                         * Save parent question
                         */
                        Question savedQuestion =
                                questionRepository.save(question);


                        /*
                         * Save all matching pairs
                         */
                        for (Map<String, String> row :
                                questionRows) {

                            QuestionMatchingPair pair =
                                    new QuestionMatchingPair();

                            pair.setQuestion(
                                    savedQuestion
                            );

                            pair.setColumnA(
                                    row.get("column_a").trim()
                            );

                            pair.setColumnB(
                                    row.get("column_b").trim()
                            );

                            pair.setDisplayOrder(
                                    Integer.parseInt(
                                            row.get("display_order")
                                                    .trim()
                                    )
                            );

                            questionMatchingPairRepository.save(
                                    pair
                            );

                            createdAttributes++;
                        }


                        uploadedQuestions++;

                        continue;
                    }


                    /*
                     * =====================================================
                     * CASE 2:
                     * Question already exists
                     *
                     * Check individual pairs.
                     * =====================================================
                     */
                    Question savedQuestion =
                            existingQuestion.get();


                    /*
                     * Get existing pairs
                     */
                    List<QuestionMatchingPair> existingPairs =
                            questionMatchingPairRepository
                                    .findByQuestion_QuestionIdOrderByDisplayOrderAsc(
                                            savedQuestion.getQuestionId()
                                    );


                    /*
                     * Build set of existing pairs.
                     *
                     * We compare:
                     * column_a + column_b + display_order
                     */
                    Set<String> existingPairKeys =
                            new HashSet<>();

                    for (QuestionMatchingPair existingPair :
                            existingPairs) {

                        String key =
                                buildPairKey(
                                        existingPair.getColumnA(),
                                        existingPair.getColumnB(),
                                        existingPair.getDisplayOrder()
                                );

                        existingPairKeys.add(key);
                    }


                    int newPairsForQuestion = 0;


                    /*
                     * Process Excel pairs individually
                     */
                    for (Map<String, String> row :
                            questionRows) {

                        String columnA =
                                row.get("column_a").trim();

                        String columnB =
                                row.get("column_b").trim();

                        int displayOrder =
                                Integer.parseInt(
                                        row.get("display_order")
                                                .trim()
                                );


                        String pairKey =
                                buildPairKey(
                                        columnA,
                                        columnB,
                                        displayOrder
                                );


                        /*
                         * Pair already exists
                         */
                        if (existingPairKeys.contains(pairKey)) {

                            skippedRows++;

                            continue;
                        }


                        /*
                         * New pair for existing question
                         */
                        QuestionMatchingPair newPair =
                                new QuestionMatchingPair();

                        newPair.setQuestion(
                                savedQuestion
                        );

                        newPair.setColumnA(
                                columnA
                        );

                        newPair.setColumnB(
                                columnB
                        );

                        newPair.setDisplayOrder(
                                displayOrder
                        );

                        questionMatchingPairRepository.save(
                                newPair
                        );

                        existingPairKeys.add(pairKey);

                        createdAttributes++;

                        newPairsForQuestion++;
                    }


                    /*
                     * If no new pairs were added,
                     * the entire question was already present.
                     */
                    if (newPairsForQuestion == 0) {

                        continue;
                    }


                    /*
                     * Existing question received
                     * one or more new pairs.
                     *
                     * We count this as uploaded question data.
                     */
                    uploadedQuestions++;

                } catch (Exception e) {

                    failedQuestions++;

                    /*
                     * Use the first Excel row of
                     * this question for the error.
                     */
                    String rowNumber =
                            questionRows.get(0)
                                    .get("_excel_row_number");

                    errors.add(
                            createError(
                                    rowNumber,
                                    e.getMessage()
                            )
                    );
                }
            }


            /*
             * Build response
             */
            response.setUploadedQuestions(
                    uploadedQuestions
            );

            response.setSkippedRows(
                    skippedRows
            );

            response.setFailedQuestions(
                    failedQuestions
            );

            response.setCreatedAttributes(
                    createdAttributes
            );

            response.setErrors(
                    errors
            );

            response.setSuccess(
                    failedQuestions == 0
            );

            response.setMessage(
                    "Matching questions upload completed. "
                            + "Uploaded: "
                            + uploadedQuestions
                            + ", Created pairs: "
                            + createdAttributes
                            + ", Skipped: "
                            + skippedRows
                            + ", Failed: "
                            + failedQuestions
            );

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    "Failed to process Excel file: "
                            + e.getMessage()
            );

            response.setErrors(errors);
        }

        return response;
    }


    /*
     * Create unique key for a matching pair.
     */
    private String buildPairKey(
            String columnA,
            String columnB,
            Integer displayOrder) {

        return columnA.trim().toLowerCase()
                + "||"
                + columnB.trim().toLowerCase()
                + "||"
                + displayOrder;
    }


    /*
     * Read Excel file
     */
    private List<Map<String, String>> readExcel(
            MultipartFile file) throws Exception {

        List<Map<String, String>> rows =
                new ArrayList<>();

        try (InputStream inputStream =
                     file.getInputStream()) {

            Workbook workbook =
                    WorkbookFactory.create(inputStream);

            Sheet sheet =
                    workbook.getSheetAt(0);

            Iterator<Row> rowIterator =
                    sheet.iterator();

            if (!rowIterator.hasNext()) {

                workbook.close();

                return rows;
            }


            /*
             * Read header row
             */
            Row headerRow =
                    rowIterator.next();

            List<String> headers =
                    new ArrayList<>();

            for (Cell cell : headerRow) {

                String header =
                        getCellValue(cell);

                header =
                        header.trim()
                                .toLowerCase()
                                .replace(" ", "_");

                headers.add(header);
            }


            /*
             * Read data rows
             */
            while (rowIterator.hasNext()) {

                Row row =
                        rowIterator.next();

                Map<String, String> data =
                        new LinkedHashMap<>();

                boolean emptyRow = true;

                for (int i = 0;
                     i < headers.size();
                     i++) {

                    Cell cell =
                            row.getCell(
                                    i,
                                    Row.MissingCellPolicy
                                            .CREATE_NULL_AS_BLANK
                            );

                    String value =
                            getCellValue(cell);

                    if (value != null &&
                            !value.trim().isEmpty()) {

                        emptyRow = false;
                    }

                    data.put(
                            headers.get(i),
                            value
                    );
                }

                if (!emptyRow) {
                    rows.add(data);
                }
            }

            workbook.close();
        }

        return rows;
    }


    /*
     * Convert Excel cell to String
     */
    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter =
                new DataFormatter();

        return formatter
                .formatCellValue(cell)
                .trim();
    }


    private QuestionUploadErrorDTO createError(
            String rowNumber,
            String message) {

        QuestionUploadErrorDTO error =
                new QuestionUploadErrorDTO();

        error.setRowNumber(
                Integer.parseInt(rowNumber)
        );

        error.setErrorMessage(
                message
        );

        return error;
    }
}