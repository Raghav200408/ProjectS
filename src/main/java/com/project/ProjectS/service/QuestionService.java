package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.processor.QuestionExcelProcessor;
import com.project.ProjectS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class QuestionService {


    private final QuestionRepository questionRepository;

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
            QuestionExcelProcessor questionExcelProcessor) {

        this.questionRepository =
                questionRepository;

        this.questionAttributeRepository =
                questionAttributeRepository;

        this.courseRepository =
                courseRepository;

        this.chapterRepository =
                chapterRepository;

        this.subjectRepository = subjectRepository;

        this.topicRepository =
                topicRepository;

        this.questionTypeRepository =
                questionTypeRepository;

        this.tableHeaderRepository =
                tableHeaderRepository;

        this.tableAttributeRepository =
                tableAttributeRepository;

        this.excelUploadService =
                excelUploadService;

        this.questionExcelProcessor =
                questionExcelProcessor;
    }


    // =========================================================
    // CREATE QUESTION
    // =========================================================

    public QuestionResponseDTO createQuestion(
            QuestionRequestDTO request) {


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

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + request.getSubjectId()));


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

        validateHierarchy(course, subject, chapter, topic);


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
                savedAttributes
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


            dto.setCourseId(
                    question.getCourse().getCourseId()
            );


            dto.setCourseName(
                    question.getCourse().getName()
            );

            dto.setSubjectId(question.getSubject().getSubjectId());
            dto.setSubjectName(question.getSubject().getSubjectName());


            dto.setChapterId(
                    question.getChapter().getChapterId()
            );


            dto.setChapterName(
                    question.getChapter().getName()
            );


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


            responseList.add(
                    convertToResponse(
                            question,
                            attributes
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


        return convertToResponse(
                question,
                attributes
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
                                    ));


            List<QuestionAttribute> attributes =
                    questionAttributeRepository
                            .findByQuestion_QuestionId(
                                    questionId
                            );


            responseList.add(
                    convertToResponse(
                            question,
                            attributes
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

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + request.getSubjectId()));


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

        validateHierarchy(course, subject, chapter, topic);


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


        questionAttributeRepository.flush();


        questionAttributeRepository
                .deleteByQuestion_QuestionId(
                        questionId
                );


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
                savedAttributes
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


            responseList.add(
                    convertToResponse(
                            question,
                            attributes
                    )
            );
        }


        return responseList;
    }


    // =========================================================
    // CONVERT TO RESPONSE
    // =========================================================

    private QuestionResponseDTO convertToResponse(
            Question question,
            List<QuestionAttribute> attributes) {


        QuestionResponseDTO response =
                new QuestionResponseDTO();

        response.setSubjectId(question.getSubject().getSubjectId());
        response.setSubjectName(question.getSubject().getSubjectName());


        response.setQuestionId(
                question.getQuestionId()
        );


        response.setQuestionText(
                question.getQuestionText()
        );


        // =====================================================
        // COURSE
        // =====================================================

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


        // =====================================================
        // CHAPTER
        // =====================================================

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


        // =====================================================
        // CATEGORY
        // =====================================================

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


        return response;
    }

    private void validateHierarchy(Course course, Subject subject, Chapter chapter, Topic topic) {
        if (!subject.getCourse().getCourseId().equals(course.getCourseId())
                || !chapter.getCourse().getCourseId().equals(course.getCourseId())
                || !chapter.getSubject().getSubjectId().equals(subject.getSubjectId())
                || !topic.getCourse().getCourseId().equals(course.getCourseId())
                || !topic.getSubject().getSubjectId().equals(subject.getSubjectId())
                || !topic.getChapter().getChapterId().equals(chapter.getChapterId())) {
            throw new RuntimeException("Course, subject, chapter, and topic must belong to the same hierarchy");
        }
    }

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
}
