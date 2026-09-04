
        package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.McqOption;
import com.project.ProjectS.entity.McqQuestion;
import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.Topic;

import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.McqOptionRepository;
import com.project.ProjectS.repository.McqQuestionRepository;
import com.project.ProjectS.repository.TopicRepository;
import com.project.ProjectS.repository.QuestionRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class McqExcelUploadProcessor {

    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final QuestionRepository questionRepository;

    private final McqQuestionRepository mcqQuestionRepository;

    private final McqOptionRepository mcqOptionRepository;

    private final CourseRepository courseRepository;

    private final ChapterRepository chapterRepository;

    private final TopicRepository topicRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public McqExcelUploadProcessor(
            QuestionRepository questionRepository,
            McqQuestionRepository mcqQuestionRepository,
            McqOptionRepository mcqOptionRepository,
            CourseRepository courseRepository,
            ChapterRepository chapterRepository,
            TopicRepository topicRepository) {

        this.questionRepository = questionRepository;

        this.mcqQuestionRepository = mcqQuestionRepository;

        this.mcqOptionRepository = mcqOptionRepository;

        this.courseRepository = courseRepository;

        this.chapterRepository = chapterRepository;

        this.topicRepository =
                topicRepository;
    }


    // =========================================================
    // PROCESS EXCEL
    // =========================================================

    @Transactional
    public int processExcel(
            MultipartFile file,
            Long courseId,
            Long chapterId,
            Long topicId
    ) throws Exception {

        int uploadedCount = 0;

        int skippedCount = 0;

        int failedCount = 0;

        int totalProcessed = 0;


        List<String> skippedQuestions =
                new ArrayList<>();

        List<String> failedQuestions =
                new ArrayList<>();

        // VALIDATE FILE

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Excel file is required"
            );
        }


        // =====================================================
        // VALIDATE FRONTEND IDs
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


        // =====================================================
        // FIND COURSE
        // =====================================================

        Optional<Course> courseOptional =
                courseRepository.findById(courseId);

        if (courseOptional.isEmpty()) {

            throw new RuntimeException(
                    "Course ID "
                            + courseId
                            + " not found"
            );
        }

        Course course =
                courseOptional.get();


        // =====================================================
        // FIND CHAPTER
        // =====================================================

        Optional<Chapter> chapterOptional =
                chapterRepository.findById(chapterId);

        if (chapterOptional.isEmpty()) {

            throw new RuntimeException(
                    "Chapter ID "
                            + chapterId
                            + " not found"
            );
        }

        Chapter chapter =
                chapterOptional.get();


        // =====================================================
        // FIND CATEGORY
        // =====================================================

        Optional<Topic> topicOptional =
                topicRepository.findById(
                        topicId
                );

        if (topicOptional.isEmpty()) {

            throw new RuntimeException(
                    "Topic ID "
                            + topicId
                            + " not found"
            );
        }

        Topic topic =
                topicOptional.get();


        // =====================================================
        // OPEN EXCEL
        // =====================================================

        try (
                InputStream inputStream =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(
                                inputStream
                        )
        ) {

            // =================================================
            // CHECK SHEETS
            // =================================================

            if (workbook.getNumberOfSheets() == 0) {

                throw new RuntimeException(
                        "Excel file does not contain any sheet"
                );
            }


            Sheet sheet =
                    workbook.getSheetAt(0);


            // =================================================
            // START LOG
            // =================================================

            System.out.println();

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "PROCESSING MCQ EXCEL"
            );

            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "File Name = "
                            + file.getOriginalFilename()
            );

            System.out.println(
                    "Course ID = "
                            + courseId
            );

            System.out.println(
                    "Chapter ID = "
                            + chapterId
            );

            System.out.println(
                    "Topic ID = "
                            + topicId
            );

            System.out.println(
                    "Total Excel Rows = "
                            + sheet.getLastRowNum()
            );

            System.out.println(
                    "=========================================="
            );


            // =================================================
            // HEADER
            // =================================================

            Row headerRow =
                    sheet.getRow(0);

            if (headerRow == null) {

                throw new RuntimeException(
                        "Excel header row is missing"
                );
            }


            // =================================================
            // PRINT HEADERS
            // =================================================

            String headers =
                    getHeaders(headerRow);

            System.out.println(
                    "Excel Headers = "
                            + headers
            );


            // =================================================
            // VALIDATE HEADERS
            // =================================================

            validateHeaders(headerRow);


            // =================================================
            // PROCESS EACH ROW
            // =================================================

            for (
                    int rowIndex = 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++
            ) {

                Row row =
                        sheet.getRow(rowIndex);


                // -------------------------------------------------
                // SKIP EMPTY ROW
                // -------------------------------------------------

                if (
                        row == null ||
                                isRowEmpty(row)
                ) {

                    continue;
                }


                totalProcessed++;


                System.out.println();

                System.out.println(
                        "------------------------------------------"
                );

                System.out.println(
                        "Processing Excel Row = "
                                + (rowIndex + 1)
                );

                System.out.println(
                        "------------------------------------------"
                );


                try {

                    // =================================================
                    // 1. QUESTION TEXT
                    // =================================================

                    String questionText =
                            getCellValue(
                                    row.getCell(0)
                            );


                    if (
                            questionText == null ||
                                    questionText.isBlank()
                    ) {

                        throw new RuntimeException(
                                "Question text is required"
                        );
                    }


                    System.out.println(
                            "Question = "
                                    + questionText
                    );


                    // =================================================
                    // 2. QUESTION TYPE
                    // =================================================

                    String questionType =
                            getCellValue(
                                    row.getCell(1)
                            );


                    if (
                            questionType == null ||
                                    questionType.isBlank()
                    ) {

                        questionType =
                                "SINGLE_CHOICE";
                    }


                    questionType =
                            questionType
                                    .trim()
                                    .toUpperCase();


                    // =================================================
                    // 3. ONLY SINGLE CHOICE
                    // =================================================

                    if (
                            !"SINGLE_CHOICE"
                                    .equals(questionType)
                    ) {

                        throw new RuntimeException(
                                "Only SINGLE_CHOICE questions "
                                        + "are supported. Found: "
                                        + questionType
                        );
                    }


                    // =================================================
                    // 4. MARKS
                    // =================================================

                    Double marks =
                            getDoubleValue(
                                    row.getCell(2),
                                    "Marks",
                                    rowIndex
                            );


                    if (marks == null) {

                        marks = 1.0;
                    }


                    if (marks <= 0) {

                        throw new RuntimeException(
                                "Marks must be greater than 0"
                        );
                    }


                    // =================================================
                    // 5. OPTION 1
                    // =================================================

                    String option1 =
                            getCellValue(
                                    row.getCell(3)
                            );


                    validateOption(
                            option1,
                            "Option 1",
                            rowIndex
                    );


                    // =================================================
                    // 6. OPTION 2
                    // =================================================

                    String option2 =
                            getCellValue(
                                    row.getCell(4)
                            );


                    validateOption(
                            option2,
                            "Option 2",
                            rowIndex
                    );


                    // =================================================
                    // 7. OPTION 3
                    // =================================================

                    String option3 =
                            getCellValue(
                                    row.getCell(5)
                            );


                    validateOption(
                            option3,
                            "Option 3",
                            rowIndex
                    );


                    // =================================================
                    // 8. OPTION 4
                    // =================================================

                    String option4 =
                            getCellValue(
                                    row.getCell(6)
                            );


                    validateOption(
                            option4,
                            "Option 4",
                            rowIndex
                    );


                    // =================================================
                    // 9. CORRECT OPTION
                    // =================================================

                    String correctOption =
                            getCellValue(
                                    row.getCell(7)
                            );


                    if (
                            correctOption == null ||
                                    correctOption.isBlank()
                    ) {

                        throw new RuntimeException(
                                "Correct option is required"
                        );
                    }


                    int correctOptionNumber;


                    try {

                        correctOptionNumber =
                                Integer.parseInt(
                                        correctOption.trim()
                                );

                    } catch (
                            NumberFormatException e
                    ) {

                        throw new RuntimeException(
                                "Correct option must be "
                                        + "a number between 1 and 4"
                        );
                    }


                    // =================================================
                    // 10. VALIDATE CORRECT OPTION
                    // =================================================

                    if (
                            correctOptionNumber < 1 ||
                                    correctOptionNumber > 4
                    ) {

                        throw new RuntimeException(
                                "Correct option must be "
                                        + "between 1 and 4"
                        );
                    }


                    // =================================================
                    // 11. CHECK EXISTING QUESTION
                    // =================================================

                    List<Question> existingQuestions =
                            questionRepository
                                    .findByCourse_CourseIdAndChapter_ChapterIdAndTopic_TopicIdAndActiveRowTrue(
                                            courseId,
                                            chapterId,
                                            topicId
                                    );


                    Question existingQuestion = null;


                    for (
                            Question existing :
                            existingQuestions
                    ) {

                        if (
                                existing.getQuestionText()
                                        != null
                                        &&
                                        existing.getQuestionText()
                                                .trim()
                                                .equalsIgnoreCase(
                                                        questionText.trim()
                                                )
                        ) {

                            existingQuestion =
                                    existing;

                            break;
                        }
                    }


                    // =================================================
                    // 12. DUPLICATE QUESTION FOUND
                    // =================================================

                    if (existingQuestion != null) {

                        Long existingQuestionId =
                                existingQuestion
                                        .getQuestionId();


                        // ---------------------------------------------
                        // Check whether MCQ already exists
                        // ---------------------------------------------

                        if (
                                mcqQuestionRepository
                                        .findById(
                                                existingQuestionId
                                        )
                                        .isPresent()
                        ) {

                            skippedCount++;


                            skippedQuestions.add(
                                    "Row "
                                            + (rowIndex + 1)
                                            + " - Question ID "
                                            + existingQuestionId
                                            + " - "
                                            + questionText
                            );


                            System.out.println(
                                    "SKIPPED - MCQ already exists"
                            );


                            continue;
                        }


                        // ---------------------------------------------
                        // Question exists but MCQ does not exist
                        // ---------------------------------------------

                        System.out.println(
                                "Existing Question found. "
                                        + "Using Question ID = "
                                        + existingQuestionId
                        );


                        McqQuestion mcqQuestion =
                                createMcqQuestion(
                                        existingQuestionId,
                                        questionType,
                                        marks
                                );


                        mcqQuestionRepository.save(
                                mcqQuestion
                        );


                        saveOptions(
                                existingQuestionId,
                                option1,
                                option2,
                                option3,
                                option4,
                                correctOptionNumber
                        );


                        uploadedCount++;


                        System.out.println(
                                "MCQ CREATED FOR EXISTING QUESTION"
                        );


                        continue;
                    }


                    // =================================================
                    // 13. CREATE NEW QUESTION
                    // =================================================

                    Question question =
                            new Question();


                    question.setCourse(
                            course
                    );


                    question.setChapter(
                            chapter
                    );


                    question.setTopic(
                            topic
                    );
                    question.setSubject(topic.getSubject());


                    question.setQuestionText(
                            questionText
                    );


                    question.setActiveRow(
                            true
                    );


                    // =================================================
                    // 14. SAVE QUESTION
                    // =================================================

                    Question savedQuestion =
                            questionRepository.save(
                                    question
                            );


                    // =================================================
                    // 15. GET AUTO GENERATED QUESTION ID
                    // =================================================

                    Long generatedQuestionId =
                            savedQuestion.getQuestionId();


                    if (generatedQuestionId == null) {

                        throw new RuntimeException(
                                "Question ID was not generated"
                        );
                    }


                    System.out.println(
                            "Generated Question ID = "
                                    + generatedQuestionId
                    );


                    // =================================================
                    // 16. CREATE MCQ QUESTION
                    // =================================================

                    McqQuestion mcqQuestion =
                            createMcqQuestion(
                                    generatedQuestionId,
                                    questionType,
                                    marks
                            );


                    // =================================================
                    // 17. SAVE MCQ QUESTION
                    // =================================================

                    mcqQuestionRepository.save(
                            mcqQuestion
                    );


                    // =================================================
                    // 18. SAVE OPTIONS
                    // =================================================

                    saveOptions(
                            generatedQuestionId,
                            option1,
                            option2,
                            option3,
                            option4,
                            correctOptionNumber
                    );


                    // =================================================
                    // 19. SUCCESS
                    // =================================================

                    uploadedCount++;


                    System.out.println(
                            "UPLOADED SUCCESSFULLY"
                    );


                    System.out.println(
                            "Question ID = "
                                    + generatedQuestionId
                    );


                    System.out.println(
                            "Question Type = "
                                    + questionType
                    );


                    System.out.println(
                            "Marks = "
                                    + marks
                    );


                    System.out.println(
                            "Correct Option = "
                                    + correctOptionNumber
                    );


                } catch (Exception e) {

                    // =================================================
                    // ROW FAILED
                    // =================================================

                    failedCount++;


                    String questionText =
                            getCellValue(
                                    row.getCell(0)
                            );


                    String failedMessage =
                            "Row "
                                    + (rowIndex + 1)
                                    + " - Question "
                                    + questionText
                                    + " - "
                                    + e.getMessage();


                    failedQuestions.add(
                            failedMessage
                    );


                    System.out.println(
                            "FAILED"
                    );


                    System.out.println(
                            "Row = "
                                    + (rowIndex + 1)
                    );


                    System.out.println(
                            "Question = "
                                    + questionText
                    );


                    System.out.println(
                            "Error = "
                                    + e.getMessage()
                    );
                }
            }
        }


        // =========================================================
        // FINAL SUMMARY
        // =========================================================

        System.out.println();

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "MCQ EXCEL UPLOAD SUMMARY"
        );

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Uploaded = "
                        + uploadedCount
        );

        System.out.println(
                "Skipped  = "
                        + skippedCount
        );

        System.out.println(
                "Failed   = "
                        + failedCount
        );

        System.out.println(
                "Total Processed = "
                        + totalProcessed
        );

        System.out.println(
                "=========================================="
        );


        // =========================================================
        // SKIPPED QUESTIONS
        // =========================================================

        if (
                !skippedQuestions.isEmpty()
        ) {

            System.out.println();

            System.out.println(
                    "SKIPPED QUESTIONS:"
            );


            for (
                    String skipped :
                    skippedQuestions
            ) {

                System.out.println(
                        "  "
                                + skipped
                );
            }
        }


        // =========================================================
        // FAILED QUESTIONS
        // =========================================================

        if (
                !failedQuestions.isEmpty()
        ) {

            System.out.println();

            System.out.println(
                    "FAILED QUESTIONS:"
            );


            for (
                    String failed :
                    failedQuestions
            ) {

                System.out.println(
                        "  "
                                + failed
                );
            }
        }


        // =========================================================
        // COMPLETION
        // =========================================================

        System.out.println();

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "MCQ Upload Completed"
        );

        System.out.println(
                "Uploaded Count: "
                        + uploadedCount
        );

        System.out.println(
                "=========================================="
        );


        return uploadedCount;
    }


    // =========================================================
    // CREATE MCQ QUESTION
    // =========================================================

    private McqQuestion createMcqQuestion(
            Long questionId,
            String questionType,
            Double marks) {

        McqQuestion mcqQuestion =
                new McqQuestion();


        /*
         * questionId comes from the questions table.
         *
         * It is NOT generated independently here.
         */

        mcqQuestion.setQuestionId(
                questionId
        );


        mcqQuestion.setQuestionType(
                questionType
        );


        mcqQuestion.setMarks(
                marks
        );


        mcqQuestion.setActiveRow(
                true
        );


        return mcqQuestion;
    }


    // =========================================================
    // SAVE ALL MCQ OPTIONS
    // =========================================================

    private void saveOptions(
            Long questionId,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOptionNumber) {


        McqOption mcqOption1 =
                createOption(
                        questionId,
                        1,
                        option1,
                        correctOptionNumber == 1
                );


        McqOption mcqOption2 =
                createOption(
                        questionId,
                        2,
                        option2,
                        correctOptionNumber == 2
                );


        McqOption mcqOption3 =
                createOption(
                        questionId,
                        3,
                        option3,
                        correctOptionNumber == 3
                );


        McqOption mcqOption4 =
                createOption(
                        questionId,
                        4,
                        option4,
                        correctOptionNumber == 4
                );


        mcqOptionRepository.saveAll(
                List.of(
                        mcqOption1,
                        mcqOption2,
                        mcqOption3,
                        mcqOption4
                )
        );
    }


    // =========================================================
    // CREATE MCQ OPTION
    // =========================================================

    private McqOption createOption(
            Long questionId,
            Integer optionOrder,
            String optionText,
            Boolean isCorrect) {

        McqOption option =
                new McqOption();


        option.setQuestionId(
                questionId
        );


        option.setOptionOrder(
                optionOrder
        );


        option.setOptionText(
                optionText
        );


        option.setIsCorrect(
                isCorrect
        );


        option.setActiveRow(
                true
        );


        return option;
    }


    // =========================================================
    // VALIDATE OPTION
    // =========================================================

    private void validateOption(
            String option,
            String optionName,
            int rowIndex) {

        if (
                option == null ||
                        option.isBlank()
        ) {

            throw new RuntimeException(
                    optionName
                            + " is required at Excel row "
                            + (rowIndex + 1)
            );
        }
    }


    // =========================================================
    // GET CELL VALUE
    // =========================================================

    private String getCellValue(
            Cell cell) {

        if (cell == null) {

            return null;
        }


        DataFormatter formatter =
                new DataFormatter();


        String value =
                formatter.formatCellValue(
                        cell
                );


        if (value == null) {

            return null;
        }


        value =
                value.trim();


        return value.isEmpty()
                ? null
                : value;
    }


    // =========================================================
    // GET DOUBLE VALUE
    // =========================================================

    private Double getDoubleValue(
            Cell cell,
            String columnName,
            int rowIndex) {

        String value =
                getCellValue(cell);


        if (
                value == null ||
                        value.isBlank()
        ) {

            return null;
        }


        try {

            return Double.parseDouble(
                    value.trim()
            );

        } catch (
                NumberFormatException e
        ) {

            throw new RuntimeException(
                    "Invalid "
                            + columnName
                            + " at Excel row "
                            + (rowIndex + 1)
                            + ": "
                            + value
            );
        }
    }


    // =========================================================
    // GET HEADERS
    // =========================================================

    private String getHeaders(
            Row headerRow) {

        StringBuilder headers =
                new StringBuilder();


        for (
                int i = 0;
                i < 8;
                i++
        ) {

            if (i > 0) {

                headers.append(", ");
            }


            String header =
                    getCellValue(
                            headerRow.getCell(i)
                    );


            headers.append(
                    header
            );
        }


        return headers.toString();
    }


    // =========================================================
    // VALIDATE HEADERS
    // =========================================================

    private void validateHeaders(
            Row headerRow) {

        String[] expectedHeaders = {

                "question_text",
                "question_type",
                "marks",
                "option_1",
                "option_2",
                "option_3",
                "option_4",
                "correct_option"
        };


        for (
                int i = 0;
                i < expectedHeaders.length;
                i++
        ) {

            String actualHeader =
                    getCellValue(
                            headerRow.getCell(i)
                    );


            if (
                    actualHeader == null ||
                            actualHeader.isBlank()
            ) {

                throw new RuntimeException(
                        "Header name is required at column "
                                + (i + 1)
                                + ". Expected: "
                                + expectedHeaders[i]
                );
            }


            if (
                    !actualHeader
                            .trim()
                            .equalsIgnoreCase(
                                    expectedHeaders[i]
                            )
            ) {

                throw new RuntimeException(
                        "Invalid header at column "
                                + (i + 1)
                                + ". Expected: "
                                + expectedHeaders[i]
                                + ", Found: "
                                + actualHeader
                );
            }
        }
    }


    // =========================================================
    // CHECK EMPTY ROW
    // =========================================================

    private boolean isRowEmpty(
            Row row) {

        for (
                int i = 0;
                i < 8;
                i++
        ) {

            String value =
                    getCellValue(
                            row.getCell(i)
                    );


            if (
                    value != null &&
                            !value.isBlank()
            ) {

                return false;
            }
        }


        return true;
    }
}

