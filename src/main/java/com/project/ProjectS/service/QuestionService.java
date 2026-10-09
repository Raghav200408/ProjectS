package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.processor.FillInTheBlankQuestionExcelProcessor;
import com.project.ProjectS.processor.QuestionExcelProcessor;
import com.project.ProjectS.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {
    private static final Logger log = LogManager.getLogger(QuestionService.class);

    static QuestionAttribute resolveUpdatedAttribute(List<QuestionAttribute> existing,
            QuestionAttributeRequestDTO request, java.util.Set<Long> retainedIds) {
        if (request.getQuestionAttributeId() != null) {
            QuestionAttribute row = existing.stream()
                    .filter(item -> request.getQuestionAttributeId().equals(item.getQuestionAttributeId()))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Question attribute does not belong to this question"));
            if (!retainedIds.add(row.getQuestionAttributeId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate question attribute ID");
            }
            return row;
        }
        // Older clients may omit row IDs; reuse only an unambiguous identity.
        List<QuestionAttribute> candidates = existing.stream()
                .filter(row -> !retainedIds.contains(row.getQuestionAttributeId()))
                .filter(row -> row.getAttribute() != null && row.getHeader() != null)
                .filter(row -> java.util.Objects.equals(row.getAttribute().getAttributeId(), request.getAttributeId())
                        && java.util.Objects.equals(row.getHeader().getHeaderId(), request.getHeaderId()))
                .collect(Collectors.toList());
        if (candidates.size() == 1) {
            QuestionAttribute row = candidates.get(0);
            retainedIds.add(row.getQuestionAttributeId());
            return row;
        }
        if (candidates.size() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ambiguous attributes: send questionAttributeId for each existing row");
        }
        return new QuestionAttribute();
    }

    static void validateEditedType(Question question, Long requestedTypeId) {
        if (question.getQuestionType() == null ||
                !java.util.Objects.equals(question.getQuestionType().getQuestionTypeId(), requestedTypeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question type cannot be changed during editing");
        }
    }

    static void validateGenericUpdate(Question question, QuestionRequestDTO request) {
        validateEditedType(question, request.getQuestionTypeId());
        String type = String.valueOf(question.getQuestionType().getQuestionType()).toUpperCase()
                .replaceAll("[\\s_-]", "");
        if ((type.contains("FILL") && type.contains("BLANK")) || type.contains("CHOICE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use the type-specific question update endpoint");
        }
    }

    // Journal/dropdown targets may intentionally use other headers. Trial-balance
    // attributes must retain the header under which the attribute was defined.
    static void validateAttributeHeader(Question question, TableHeader header, TableAttribute attribute) {
        String type = question.getQuestionType() == null ? "" :
                question.getQuestionType().getQuestionType();
        if (type == null || !type.replaceAll("[\\s_-]", "").equalsIgnoreCase("DRAGANDDROP")) {
            return;
        }
        TableHeader expected = attribute.getTableHeader();
        if (expected == null || expected.getHeaderId() == null ||
                !expected.getHeaderId().equals(header.getHeaderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Header does not match attribute: " + attribute.getName());
        }
    }

    private final QuestionRepository questionRepository;

    private final McqOptionRepository mcqOptionRepository;

    private final QuestionAttributeRepository questionAttributeRepository;

    private final CourseRepository courseRepository;

    private final ChapterRepository chapterRepository;

    private final SubjectRepository subjectRepository;

    private final QuestionTypeRepository questionTypeRepository;

    private final TopicRepository topicRepository;

    private final TableHeaderRepository tableHeaderRepository;

    private final TableAttributeRepository tableAttributeRepository;

    private final ExcelUploadService excelUploadService;

    private final QuestionExcelProcessor questionExcelProcessor;

    private final SubscriptionEntitlementService entitlementService;

    // MATCH THE FOLLOWING
    private final QuestionMatchingPairRepository questionMatchingPairRepository;

    // FILL IN THE BLANK
    private final QuestionFillBlankAnswerRepository questionFillBlankAnswerRepository;

    private final FillInTheBlankQuestionExcelProcessor fillInTheBlankQuestionExcelProcessor;


    @Autowired
    public QuestionService(
            QuestionRepository questionRepository,
            QuestionAttributeRepository questionAttributeRepository,
            CourseRepository courseRepository,
            ChapterRepository chapterRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            QuestionTypeRepository questionTypeRepository,
            TableHeaderRepository tableHeaderRepository,
            TableAttributeRepository tableAttributeRepository,
            ExcelUploadService excelUploadService,
            QuestionExcelProcessor questionExcelProcessor,
            McqOptionRepository mcqOptionRepository,
            SubscriptionEntitlementService entitlementService,
            QuestionMatchingPairRepository questionMatchingPairRepository,
            QuestionFillBlankAnswerRepository questionFillBlankAnswerRepository,
            FillInTheBlankQuestionExcelProcessor fillInTheBlankQuestionExcelProcessor) {

        this.questionRepository = questionRepository;

        this.questionAttributeRepository = questionAttributeRepository;

        this.courseRepository = courseRepository;

        this.chapterRepository = chapterRepository;

        this.subjectRepository = subjectRepository;

        this.mcqOptionRepository = mcqOptionRepository;

        this.topicRepository = topicRepository;

        this.questionTypeRepository = questionTypeRepository;

        this.tableHeaderRepository = tableHeaderRepository;

        this.tableAttributeRepository = tableAttributeRepository;

        this.excelUploadService = excelUploadService;

        this.questionExcelProcessor = questionExcelProcessor;

        this.entitlementService = entitlementService;

        // MATCH THE FOLLOWING
        this.questionMatchingPairRepository =
                questionMatchingPairRepository;

        // FILL IN THE BLANK
        this.questionFillBlankAnswerRepository =
                questionFillBlankAnswerRepository;

        this.fillInTheBlankQuestionExcelProcessor =
                fillInTheBlankQuestionExcelProcessor;
    }


    public void requireCourseAccess(Long courseId, String email) {
        entitlementService.requireCourseAccess(email, courseId);
    }


    // =========================================================
    // CREATE QUESTION
    // =========================================================

    public QuestionResponseDTO createQuestion(
            QuestionRequestDTO request) {
        log.info("Creating question: courseId={} chapterId={} subjectId={} topicId={} typeId={}",
                request.getCourseId(), request.getChapterId(), request.getSubjectId(),
                request.getTopicId(), request.getQuestionTypeId());

        Course course =
                courseRepository
                        .findById(
                                request.getCourseId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found with id: "
                                                + request.getCourseId()
                                )
                        );


        Chapter chapter =
                chapterRepository
                        .findById(
                                request.getChapterId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chapter not found with id: "
                                                + request.getChapterId()
                                )
                        );


        Subject subject =
                subjectRepository
                        .findById(
                                request.getSubjectId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subject not found with id: "
                                                + request.getSubjectId()
                                )
                        );


        Topic topic =
                topicRepository
                        .findById(
                                request.getTopicId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question topic not found with id: "
                                                + request.getTopicId()
                                )
                        );


        validateHierarchy(
                course,
                subject,
                chapter,
                topic
        );


        QuestionType questionType =
                getQuestionType(
                        request.getQuestionTypeId()
                );


        Question question =
                new Question();


        question.setCourse(course);

        question.setSubject(subject);

        question.setChapter(chapter);

        question.setTopic(topic);

        question.setQuestionType(questionType);

        question.setQuestionText(
                request.getQuestionText()
        );


        Question savedQuestion =
                questionRepository.save(
                        question
                );
        log.info("Question created successfully: questionId={} questionTypeId={}",
                savedQuestion.getQuestionId(), savedQuestion.getQuestionType().getQuestionTypeId());

        // =========================================================
        // MATCHING PAIRS - MATCH THE FOLLOWING
        // =========================================================

        List<QuestionMatchingPair> savedPairs =
                new ArrayList<>();


        if (request.getPairs() != null) {

            for (MatchingPairRequestDTO pairRequest
                    : request.getPairs()) {

                QuestionMatchingPair matchingPair =
                        new QuestionMatchingPair();


                matchingPair.setQuestion(
                        savedQuestion
                );


                matchingPair.setColumnA(
                        pairRequest.getColumnA()
                );


                matchingPair.setColumnB(
                        pairRequest.getColumnB()
                );


                matchingPair.setDisplayOrder(
                        pairRequest.getDisplayOrder()
                );


                QuestionMatchingPair savedPair =
                        questionMatchingPairRepository.save(
                                matchingPair
                        );


                savedPairs.add(
                        savedPair
                );
            }
        }


        // =========================================================
        // FILL IN THE BLANK ANSWERS
        // =========================================================

        List<QuestionFillBlankAnswer> savedBlanks =
                new ArrayList<>();


        if (request.getBlanks() != null) {

            for (FillInTheBlankAnswerRequestDTO blankRequest
                    : request.getBlanks()) {

                QuestionFillBlankAnswer blankAnswer =
                        new QuestionFillBlankAnswer();


                blankAnswer.setQuestion(
                        savedQuestion
                );


                blankAnswer.setAnswerText(
                        blankRequest.getAnswerText()
                );


                blankAnswer.setDisplayOrder(
                        blankRequest.getDisplayOrder()
                );


                QuestionFillBlankAnswer savedBlank =
                        questionFillBlankAnswerRepository.save(
                                blankAnswer
                        );


                savedBlanks.add(
                        savedBlank
                );
            }
        }


        // =========================================================
        // QUESTION ATTRIBUTES
        // =========================================================

        List<QuestionAttribute> savedAttributes =
                new ArrayList<>();


        if (request.getQuestionAttributes() != null) {

            for (QuestionAttributeRequestDTO attributeRequest
                    : request.getQuestionAttributes()) {


                TableHeader header =
                        tableHeaderRepository
                                .findById(
                                        attributeRequest.getHeaderId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Header not found with id: "
                                                        + attributeRequest.getHeaderId()
                                        )
                                );


                TableAttribute attribute =
                        tableAttributeRepository
                                .findById(
                                        attributeRequest.getAttributeId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Attribute not found with id: "
                                                        + attributeRequest.getAttributeId()
                                        )
                                );


                QuestionAttribute questionAttribute =
                        new QuestionAttribute();


                questionAttribute.setQuestion(
                        savedQuestion
                );


                validateAttributeHeader(savedQuestion, header, attribute);
                questionAttribute.setHeader(header);

                questionAttribute.setAttribute(attribute);


                questionAttribute.setTransactionDate(
                        attributeRequest.getTransactionDate()
                );


                questionAttribute.setAmount(
                        attributeRequest.getAmount()
                );


                questionAttribute.setAmount2(
                        attributeRequest.getAmount2()
                );


                questionAttribute.setNote(
                        attributeRequest.getNote()
                );


                QuestionAttribute savedAttribute =
                        questionAttributeRepository.save(
                                questionAttribute
                        );


                savedAttributes.add(
                        savedAttribute
                );
            }
        }


        return convertToResponse(
                savedQuestion,
                savedAttributes,
                savedPairs,
                savedBlanks
        );
    }


    // =========================================================
    // EXCEL UPLOAD
    // =========================================================

    public QuestionExcelUploadResponseDTO uploadQuestions(
            MultipartFile file,
            Integer courseId,
            Integer chapterId,
            Integer topicId) {


        // =====================================================
        // FILE VALIDATION
        // =====================================================

        if (file == null ||
                file.isEmpty()) {

            throw new RuntimeException(
                    "Excel file is empty"
            );
        }


        // =====================================================
        // ID VALIDATION
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


        try {

            // =================================================
            // READ EXCEL
            // =================================================

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(
                            file
                    );


            if (excelData == null ||
                    excelData.isEmpty()) {

                throw new RuntimeException(
                        "Excel file contains no data"
                );
            }


            // =================================================
            // PROCESS QUESTIONS
            // =================================================

            return questionExcelProcessor.process(
                    excelData,
                    topicId,
                    chapterId,
                    courseId
            );


        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to read Excel file: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // =========================================================
    // GET ALL QUESTION TEXT
    // =========================================================

    public List<QuestionResponseDTO> getAllQuestionText() {

        List<Question> questions =
                questionRepository.findAll();


        List<QuestionResponseDTO> response =
                new ArrayList<>();


        for (Question question : questions) {

            QuestionResponseDTO dto =
                    new QuestionResponseDTO();


            dto.setQuestionId(
                    question.getQuestionId()
            );


            dto.setQuestionText(
                    question.getQuestionText()
            );


            // COURSE

            if (question.getCourse() != null) {

                dto.setCourseId(
                        question
                                .getCourse()
                                .getCourseId()
                );


                dto.setCourseName(
                        question
                                .getCourse()
                                .getName()
                );
            }


            // SUBJECT

            if (question.getSubject() != null) {

                dto.setSubjectId(
                        question
                                .getSubject()
                                .getSubjectId()
                );


                dto.setSubjectName(
                        question
                                .getSubject()
                                .getSubjectName()
                );
            }


            // CHAPTER

            if (question.getChapter() != null) {

                dto.setChapterId(
                        question
                                .getChapter()
                                .getChapterId()
                );


                dto.setChapterName(
                        question
                                .getChapter()
                                .getName()
                );
            }


            // TOPIC

            if (question.getTopic() != null) {

                dto.setTopicId(
                        question
                                .getTopic()
                                .getTopicId()
                );


                dto.setTopicName(
                        question
                                .getTopic()
                                .getName()
                );
            }


            if (question.getQuestionType() != null) {

                dto.setQuestionTypeId(
                        question
                                .getQuestionType()
                                .getQuestionTypeId()
                );


                dto.setQuestionType(
                        question
                                .getQuestionType()
                                .getQuestionType()
                );
            }


            dto.setActiveRow(
                    question.getActiveRow()
            );


            response.add(dto);
        }


        return response;
    }


    // =========================================================
    // GET ALL QUESTIONS
    // =========================================================

    public List<QuestionResponseDTO> getAllQuestions() {

        List<Question> questions =
                questionRepository
                        .findByActiveRowTrue();


        List<QuestionResponseDTO> responseList =
                new ArrayList<>();


        for (Question question : questions) {

            List<QuestionAttribute> attributes =
                    questionAttributeRepository
                            .findByQuestion_QuestionId(
                                    question.getQuestionId()
                            );


            // MATCH THE FOLLOWING

            List<QuestionMatchingPair> pairs =
                    questionMatchingPairRepository
                            .findByQuestion_QuestionIdOrderByDisplayOrderAsc(
                                    question.getQuestionId()
                            );


            // FILL IN THE BLANK

            List<QuestionFillBlankAnswer> blanks =
                    questionFillBlankAnswerRepository
                            .findByQuestionQuestionIdOrderByDisplayOrderAsc(
                                    question.getQuestionId()
                            );


            responseList.add(
                    convertToResponse(
                            question,
                            attributes,
                            pairs,
                            blanks
                    )
            );
        }


        return responseList;
    }


    // =========================================================
    // GET QUESTION BY ID
    // =========================================================

    public QuestionResponseDTO getQuestionById(
            Long questionId) {

        Question question =
                questionRepository
                        .findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found with id: "
                                                + questionId
                                )
                        );


        List<QuestionAttribute> attributes =
                questionAttributeRepository
                        .findByQuestion_QuestionId(
                                questionId
                        );


        // MATCH THE FOLLOWING

        List<QuestionMatchingPair> pairs =
                questionMatchingPairRepository
                        .findByQuestion_QuestionIdOrderByDisplayOrderAsc(
                                questionId
                        );


        // FILL IN THE BLANK

        List<QuestionFillBlankAnswer> blanks =
                questionFillBlankAnswerRepository
                        .findByQuestionQuestionIdOrderByDisplayOrderAsc(
                                questionId
                        );


        return convertToResponse(
                question,
                attributes,
                pairs,
                blanks
        );
    }


    // =========================================================
    // GET QUESTIONS BY IDS
    // =========================================================

    public List<QuestionResponseDTO> getQuestionsByIds(
            List<Long> questionIds) {

        List<QuestionResponseDTO> responseList =
                new ArrayList<>();


        for (Long questionId : questionIds) {

            Question question =
                    questionRepository
                            .findById(questionId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Question not found with id: "
                                                    + questionId
                                    )
                            );

            // if it's mcq
            List<McqOption> options =
                    mcqOptionRepository
                            .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                    questionId
                            );

            List<QuestionAttribute> attributes =
                    questionAttributeRepository
                            .findByQuestion_QuestionId(
                                    questionId
                            );


            // MATCH THE FOLLOWING

            List<QuestionMatchingPair> pairs =
                    questionMatchingPairRepository
                            .findByQuestion_QuestionIdOrderByDisplayOrderAsc(
                                    questionId
                            );


            // FILL IN THE BLANK

            List<QuestionFillBlankAnswer> blanks =
                    questionFillBlankAnswerRepository
                            .findByQuestionQuestionIdOrderByDisplayOrderAsc(
                                    questionId
                            );


            responseList.add(
                    convertToResponse(
                            question,
                            attributes,
                            pairs,
                            blanks
                    )
            );
        }


        return responseList;
    }


    // =========================================================
    // UPDATE QUESTION
    // =========================================================

    public QuestionResponseDTO updateQuestion(
            Long questionId,
            QuestionRequestDTO request) {


        Question question =
                questionRepository
                        .findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found with id: "
                                                + questionId
                                )
                        );


        validateGenericUpdate(question, request);

        Course course =
                courseRepository
                        .findById(
                                request.getCourseId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found with id: "
                                                + request.getCourseId()
                                )
                        );


        Chapter chapter =
                chapterRepository
                        .findById(
                                request.getChapterId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chapter not found with id: "
                                                + request.getChapterId()
                                )
                        );


        Subject subject =
                subjectRepository
                        .findById(
                                request.getSubjectId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subject not found with id: "
                                                + request.getSubjectId()
                                )
                        );


        Topic topic =
                topicRepository
                        .findById(
                                request.getTopicId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question topic not found with id: "
                                                + request.getTopicId()
                                )
                        );


        validateHierarchy(
                course,
                subject,
                chapter,
                topic
        );


        QuestionType questionType =
                getQuestionType(
                        request.getQuestionTypeId()
                );


        question.setCourse(course);

        question.setSubject(subject);

        question.setChapter(chapter);

        question.setTopic(topic);

        question.setQuestionType(questionType);

        question.setQuestionText(
                request.getQuestionText()
        );


        Question savedQuestion =
                questionRepository.save(
                        question
                );


        // =========================================================
        // DELETE OLD QUESTION ATTRIBUTES
        // =========================================================

        List<QuestionAttribute> existingAttributes =
                questionAttributeRepository.findByQuestion_QuestionId(questionId);
        java.util.Set<Long> retainedAttributeIds = new java.util.HashSet<>();


        // =========================================================
        // DELETE OLD MATCHING PAIRS
        // =========================================================

        questionMatchingPairRepository
                .deleteByQuestion_QuestionId(
                        questionId
                );


        // =========================================================
        // DELETE OLD FILL IN THE BLANK ANSWERS
        // =========================================================

        questionFillBlankAnswerRepository
                .deleteByQuestionQuestionId(
                        questionId
                );


        // =========================================================
        // SAVE QUESTION ATTRIBUTES
        // =========================================================

        List<QuestionAttribute> savedAttributes =
                new ArrayList<>();


        if (request.getQuestionAttributes() != null) {

            for (QuestionAttributeRequestDTO attributeRequest
                    : request.getQuestionAttributes()) {


                TableHeader header =
                        tableHeaderRepository
                                .findById(
                                        attributeRequest.getHeaderId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Header not found with id: "
                                                        + attributeRequest.getHeaderId()
                                        )
                                );


                TableAttribute attribute =
                        tableAttributeRepository
                                .findById(
                                        attributeRequest.getAttributeId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Attribute not found with id: "
                                                        + attributeRequest.getAttributeId()
                                        )
                                );


                QuestionAttribute questionAttribute = resolveUpdatedAttribute(
                        existingAttributes, attributeRequest, retainedAttributeIds);
                questionAttribute.setQuestion(savedQuestion);


                validateAttributeHeader(savedQuestion, header, attribute);
                questionAttribute.setHeader(header);

                questionAttribute.setAttribute(attribute);


                questionAttribute.setTransactionDate(
                        attributeRequest.getTransactionDate()
                );


                questionAttribute.setAmount(
                        attributeRequest.getAmount()
                );


                questionAttribute.setAmount2(
                        attributeRequest.getAmount2()
                );


                questionAttribute.setNote(
                        attributeRequest.getNote()
                );


                QuestionAttribute savedAttribute =
                        questionAttributeRepository.save(
                                questionAttribute
                        );


                savedAttributes.add(
                        savedAttribute
                );
            }
        }


        // =========================================================
        // SAVE MATCHING PAIRS
        // =========================================================

        questionAttributeRepository.deleteAll(existingAttributes.stream()
                .filter(row -> !retainedAttributeIds.contains(row.getQuestionAttributeId()))
                .collect(Collectors.toList()));

        List<QuestionMatchingPair> savedPairs =
                new ArrayList<>();


        if (request.getPairs() != null) {

            for (MatchingPairRequestDTO pairRequest
                    : request.getPairs()) {

                QuestionMatchingPair matchingPair =
                        new QuestionMatchingPair();


                matchingPair.setQuestion(
                        savedQuestion
                );


                matchingPair.setColumnA(
                        pairRequest.getColumnA()
                );


                matchingPair.setColumnB(
                        pairRequest.getColumnB()
                );


                matchingPair.setDisplayOrder(
                        pairRequest.getDisplayOrder()
                );


                QuestionMatchingPair savedPair =
                        questionMatchingPairRepository.save(
                                matchingPair
                        );


                savedPairs.add(
                        savedPair
                );
            }
        }


        // =========================================================
        // SAVE FILL IN THE BLANK ANSWERS
        // =========================================================

        List<QuestionFillBlankAnswer> savedBlanks =
                new ArrayList<>();


        if (request.getBlanks() != null) {

            for (FillInTheBlankAnswerRequestDTO blankRequest
                    : request.getBlanks()) {

                QuestionFillBlankAnswer blankAnswer =
                        new QuestionFillBlankAnswer();


                blankAnswer.setQuestion(
                        savedQuestion
                );


                blankAnswer.setAnswerText(
                        blankRequest.getAnswerText()
                );


                blankAnswer.setDisplayOrder(
                        blankRequest.getDisplayOrder()
                );


                QuestionFillBlankAnswer savedBlank =
                        questionFillBlankAnswerRepository.save(
                                blankAnswer
                        );


                savedBlanks.add(
                        savedBlank
                );
            }
        }


        return convertToResponse(
                savedQuestion,
                savedAttributes,
                savedPairs,
                savedBlanks
        );
    }


    // =========================================================
    // DELETE QUESTION
    // =========================================================

    public String deleteQuestion(
            Long questionId) {

        Question question =
                questionRepository
                        .findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found with id: "
                                                + questionId
                                )
                        );


        question.setActiveRow(false);


        questionRepository.save(
                question
        );


        return "Question deleted successfully.";
    }


    // =========================================================
    // FILTER QUESTIONS
    // =========================================================

    public List<QuestionResponseDTO> getQuestionsByMapping(
            Long courseId,
            Long chapterId,
            Long topicId) {


        List<Question> questions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdAndTopic_TopicIdAndActiveRowTrue(
                                courseId,
                                chapterId,
                                topicId
                        );


        List<QuestionResponseDTO> responseList =
                new ArrayList<>();


        for (Question question : questions) {

            List<QuestionAttribute> attributes =
                    questionAttributeRepository
                            .findByQuestion_QuestionId(
                                    question.getQuestionId()
                            );


            // MATCH THE FOLLOWING

            List<QuestionMatchingPair> pairs =
                    questionMatchingPairRepository
                            .findByQuestion_QuestionIdOrderByDisplayOrderAsc(
                                    question.getQuestionId()
                            );


            // FILL IN THE BLANK

            List<QuestionFillBlankAnswer> blanks =
                    questionFillBlankAnswerRepository
                            .findByQuestionQuestionIdOrderByDisplayOrderAsc(
                                    question.getQuestionId()
                            );


            responseList.add(
                    convertToResponse(
                            question,
                            attributes,
                            pairs,
                            blanks
                    )
            );
        }


        return responseList;
    }


    private McqOptionDTO mapOptionToDTO(
            McqOption option
    ) {

        McqOptionDTO dto =
                new McqOptionDTO();

        dto.setOptionId(
                option.getOptionId()
        );

        dto.setOptionOrder(
                option.getOptionOrder()
        );

        dto.setOptionText(
                option.getOptionText()
        );

        // This path (getQuestionsByIds) only ever backs an exam or mock-exam
        // paper - never send isCorrect here, or the correct option is sitting
        // in the network tab before the student answers.
        return dto;
    }

    // =========================================================
    // CONVERT TO RESPONSE
    // =========================================================
    private QuestionResponseDTO convertToResponse(
            Question question,
            List<QuestionAttribute> attributes,
            List<QuestionMatchingPair> pairs,
            List<QuestionFillBlankAnswer> blanks) {


        QuestionResponseDTO response =
                new QuestionResponseDTO();


        // =====================================================
        // SUBJECT
        // =====================================================

        if (question.getSubject() != null) {

            response.setSubjectId(
                    question
                            .getSubject()
                            .getSubjectId()
            );


            response.setSubjectName(
                    question
                            .getSubject()
                            .getSubjectName()
            );
        }


        // =====================================================
        // QUESTION
        // =====================================================

        response.setQuestionId(
                question.getQuestionId()
        );


        response.setQuestionText(
                question.getQuestionText()
        );


        // =====================================================
        // COURSE
        // =====================================================

        if (question.getCourse() != null) {

            response.setCourseId(
                    question
                            .getCourse()
                            .getCourseId()
            );


            response.setCourseName(
                    question
                            .getCourse()
                            .getName()
            );
        }


        // =====================================================
        // CHAPTER
        // =====================================================

        if (question.getChapter() != null) {

            response.setChapterId(
                    question
                            .getChapter()
                            .getChapterId()
            );


            response.setChapterName(
                    question
                            .getChapter()
                            .getName()
            );
        }


        // =====================================================
        // TOPIC
        // =====================================================

        if (question.getTopic() != null) {

            response.setTopicId(
                    question
                            .getTopic()
                            .getTopicId()
            );


            response.setTopicName(
                    question
                            .getTopic()
                            .getName()
            );
        }


        // =====================================================
        // QUESTION TYPE
        // =====================================================

        if (question.getQuestionType() != null) {

            response.setQuestionTypeId(
                    question
                            .getQuestionType()
                            .getQuestionTypeId()
            );


            response.setQuestionType(
                    question
                            .getQuestionType()
                            .getQuestionType()
            );
        }


        response.setActiveRow(
                question.getActiveRow()
        );


        response.setCreatedAt(
                question.getCreatedAt()
        );


        response.setUpdatedAt(
                question.getUpdatedAt()
        );


        // =====================================================
        // QUESTION ATTRIBUTES
        // =====================================================

        List<QuestionAttributeResponseDTO>
                attributeResponses =
                new ArrayList<>();


        for (QuestionAttribute questionAttribute
                : attributes) {


            QuestionAttributeResponseDTO
                    attributeResponse =
                    new QuestionAttributeResponseDTO();


            attributeResponse.setQuestionAttributeId(
                    questionAttribute
                            .getQuestionAttributeId()
            );


            // =================================================
            // HEADER
            // =================================================

            if (questionAttribute.getHeader() != null) {

                attributeResponse.setHeaderId(
                        questionAttribute
                                .getHeader()
                                .getHeaderId()
                );


                attributeResponse.setHeaderName(
                        questionAttribute
                                .getHeader()
                                .getName()
                );
            }


            // =================================================
            // ATTRIBUTE
            // =================================================

            if (questionAttribute.getAttribute() != null) {

                attributeResponse.setAttributeId(
                        questionAttribute
                                .getAttribute()
                                .getAttributeId()
                );


                attributeResponse.setAttributeName(
                        questionAttribute
                                .getAttribute()
                                .getName()
                );
                TableHeader attributeHeader = questionAttribute.getAttribute().getTableHeader();
                if (attributeHeader != null) {
                    attributeResponse.setAttributeHeaderId(attributeHeader.getHeaderId());
                    attributeResponse.setAttributeHeaderName(attributeHeader.getName());
                }
            }


            attributeResponse.setTransactionDate(
                    questionAttribute
                            .getTransactionDate()
            );


            attributeResponse.setAmount(
                    questionAttribute
                            .getAmount()
            );


            attributeResponse.setAmount2(
                    questionAttribute
                            .getAmount2()
            );


            attributeResponse.setNote(
                    questionAttribute
                            .getNote()
            );


            attributeResponse.setActiveRow(
                    questionAttribute
                            .getActiveRow()
            );


            attributeResponses.add(
                    attributeResponse
            );
        }


        response.setQuestionAttributes(
                attributeResponses
        );

        // =====================================================
        // MCQ OPTIONS
        // =====================================================

        List<McqOption> options =
                mcqOptionRepository
                        .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                question.getQuestionId()
                        );

        List<McqOptionDTO> optionDTOs =
                options.stream()
                        .map(this::mapOptionToDTO)
                        .collect(Collectors.toList());

        response.setOptions(optionDTOs);


        // =====================================================
        // MATCHING PAIRS - MATCH THE FOLLOWING
        // =====================================================

        List<MatchingPairResponseDTO>
                pairResponses =
                new ArrayList<>();


        if (pairs != null) {

            for (QuestionMatchingPair pair : pairs) {

                MatchingPairResponseDTO pairResponse =
                        new MatchingPairResponseDTO();


                pairResponse.setPairId(
                        pair.getPairId()
                );


                pairResponse.setColumnA(
                        pair.getColumnA()
                );


                pairResponse.setColumnB(
                        pair.getColumnB()
                );


                pairResponse.setDisplayOrder(
                        pair.getDisplayOrder()
                );


                pairResponses.add(
                        pairResponse
                );
            }
        }


        response.setPairs(
                pairResponses
        );


        // =====================================================
        // FILL IN THE BLANK ANSWERS
        // =====================================================

        List<FillInTheBlankAnswerResponseDTO>
                blankResponses =
                new ArrayList<>();


        if (blanks != null) {

            for (QuestionFillBlankAnswer blank : blanks) {

                FillInTheBlankAnswerResponseDTO blankResponse =
                        new FillInTheBlankAnswerResponseDTO();


                blankResponse.setAnswerId(
                        blank.getAnswerId()
                );


                blankResponse.setAnswerText(
                        blank.getAnswerText()
                );


                blankResponse.setDisplayOrder(
                        blank.getDisplayOrder()
                );


                blankResponses.add(
                        blankResponse
                );
            }
        }


        response.setBlanks(
                blankResponses
        );


        return response;
    }


    // =========================================================
    // VALIDATE HIERARCHY
    // =========================================================

    private void validateHierarchy(
            Course course,
            Subject subject,
            Chapter chapter,
            Topic topic) {


        if (!subject.getCourse()
                .getCourseId()
                .equals(course.getCourseId())

                || !chapter.getCourse()
                .getCourseId()
                .equals(course.getCourseId())

                || !chapter.getSubject()
                .getSubjectId()
                .equals(subject.getSubjectId())

                || !topic.getCourse()
                .getCourseId()
                .equals(course.getCourseId())

                || !topic.getSubject()
                .getSubjectId()
                .equals(subject.getSubjectId())

                || !topic.getChapter()
                .getChapterId()
                .equals(chapter.getChapterId())) {


            throw new RuntimeException(
                    "Course, subject, chapter, and topic must belong to the same hierarchy"
            );
        }
    }


    // =========================================================
    // GET QUESTION TYPE
    // =========================================================

    private QuestionType getQuestionType(
            Long questionTypeId) {


        if (questionTypeId == null) {
            return null;
        }


        return questionTypeRepository
                .findById(questionTypeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question Type not found with id: "
                                        + questionTypeId
                        )
                );
    }

    // =========================================================
// FILL-IN-THE-BLANKS EXCEL UPLOAD
// =========================================================

    public QuestionExcelUploadResponseDTO
    uploadFillInTheBlankQuestions(
            MultipartFile file,
            Integer courseId,
            Integer chapterId,
            Integer topicId) {

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Excel file is empty"
            );
        }

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

        try {

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);

            if (excelData == null ||
                    excelData.isEmpty()) {

                throw new RuntimeException(
                        "Excel file contains no data"
                );
            }

            return fillInTheBlankQuestionExcelProcessor.process(
                    excelData,
                    topicId,
                    chapterId,
                    courseId
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to read Excel file: "
                            + e.getMessage(),
                    e
            );
        }
    }
}
