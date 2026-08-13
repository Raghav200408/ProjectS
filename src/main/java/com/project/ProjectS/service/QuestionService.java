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

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionAttributeRepository questionAttributeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;

    @Autowired
    private TableHeaderRepository tableHeaderRepository;

    @Autowired
    private TableAttributeRepository tableAttributeRepository;

    @Autowired
    private ExcelUploadService excelUploadService;

    @Autowired
    private QuestionExcelProcessor questionExcelProcessor;


    // =========================================================
    // CREATE QUESTION
    // =========================================================

    public QuestionResponseDTO createQuestion(
            QuestionRequestDTO request) {

        // Get Course
        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found with id: "
                                        + request.getCourseId()
                        )
                );

        // Get Chapter
        Chapter chapter = chapterRepository
                .findById(request.getChapterId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Chapter not found with id: "
                                        + request.getChapterId()
                        )
                );

        // Get Question Category
        QuestionCategory category =
                questionCategoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question category not found with id: "
                                                + request.getCategoryId()
                                )
                        );


        // Create Question
        Question question = new Question();

        question.setCourse(course);
        question.setChapter(chapter);
        question.setQuestionCategory(category);
        question.setQuestionText(request.getQuestionText());

        Question savedQuestion =
                questionRepository.save(question);


        // Save Question Attributes
        List<QuestionAttribute> savedAttributes =
                new ArrayList<>();

        if (request.getQuestionAttributes() != null) {

            for (QuestionAttributeRequestDTO attributeRequest
                    : request.getQuestionAttributes()) {

                TableHeader header =
                        tableHeaderRepository
                                .findById(attributeRequest.getHeaderId())
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Header not found with id: "
                                                        + attributeRequest.getHeaderId()
                                        )
                                );

                TableAttribute attribute =
                        tableAttributeRepository
                                .findById(attributeRequest.getAttributeId())
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Attribute not found with id: "
                                                        + attributeRequest.getAttributeId()
                                        )
                                );

                QuestionAttribute questionAttribute =
                        new QuestionAttribute();

                questionAttribute.setQuestion(savedQuestion);

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

                savedAttributes.add(savedAttribute);
            }
        }

        return convertToResponse(
                savedQuestion,
                savedAttributes
        );
    }


    // =========================================================
    // EXCEL UPLOAD QUESTIONS
    // =========================================================

    public QuestionExcelUploadResponseDTO uploadQuestions(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Excel file is empty"
            );
        }

        try {

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);

            if (excelData == null || excelData.isEmpty()) {

                throw new RuntimeException(
                        "Excel file contains no data"
                );
            }

            return questionExcelProcessor.process(
                    excelData
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

        List<Question> questions = questionRepository.findAll();

        List<QuestionResponseDTO> response = new ArrayList<>();

        for (Question question : questions) {

            QuestionResponseDTO dto = new QuestionResponseDTO();

            dto.setQuestionId(question.getQuestionId());
            dto.setQuestionText(question.getQuestionText());

            dto.setCourseId(
                    question.getCourse().getCourseId()
            );

            dto.setCourseName(
                    question.getCourse().getName()
            );

            // Chapter
            dto.setChapterId(
                    question.getChapter().getChapterId()
            );

            dto.setChapterName(
                    question.getChapter().getName()
            );

            // Category
            dto.setCategoryId(
                    question.getQuestionCategory().getCategoryId()
            );

            dto.setCategoryName(
                    question.getQuestionCategory().getName()
            );

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
                questionRepository.findByActiveRowTrue();

        List<QuestionResponseDTO> responseList =
                new ArrayList<>();

        for (Question question : questions) {

            List<QuestionAttribute> attributes =
                    questionAttributeRepository
                            .findByQuestion_QuestionId(
                                    question.getQuestionId()
                            );

            QuestionResponseDTO response =
                    convertToResponse(
                            question,
                            attributes
                    );

            responseList.add(response);
        }

        return responseList;
    }


    // =========================================================
    // GET QUESTION BY ID
    // =========================================================

    public QuestionResponseDTO getQuestionById(
            Long questionId) {

        Question question =
                questionRepository.findById(questionId)
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
    // UPDATE QUESTION
    // =========================================================

    public QuestionResponseDTO updateQuestion(
            Long questionId,
            QuestionRequestDTO request) {

        // Get existing question
        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: "
                                        + questionId
                        )
                );


        // Get Course
        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found with id: "
                                        + request.getCourseId()
                        )
                );


        // Get Chapter
        Chapter chapter = chapterRepository
                .findById(request.getChapterId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Chapter not found with id: "
                                        + request.getChapterId()
                        )
                );


        // Get Category
        QuestionCategory category =
                questionCategoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question category not found with id: "
                                                + request.getCategoryId()
                                )
                        );


        // Update Question
        question.setCourse(course);
        question.setChapter(chapter);
        question.setQuestionCategory(category);
        question.setQuestionText(
                request.getQuestionText()
        );

        Question savedQuestion =
                questionRepository.save(question);


        questionAttributeRepository.flush();

        questionAttributeRepository
                .deleteByQuestion_QuestionId(questionId);


        // Create updated Question Attributes
        List<QuestionAttribute> savedAttributes =
                new ArrayList<>();

        if (request.getQuestionAttributes() != null) {

            for (QuestionAttributeRequestDTO attributeRequest
                    : request.getQuestionAttributes()) {

                TableHeader header =
                        tableHeaderRepository
                                .findById(attributeRequest.getHeaderId())
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Header not found with id: "
                                                        + attributeRequest.getHeaderId()
                                        )
                                );

                TableAttribute attribute =
                        tableAttributeRepository
                                .findById(attributeRequest.getAttributeId())
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

                savedAttributes.add(savedAttribute);
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

    public String deleteQuestion(Long questionId) {

        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: "
                                        + questionId
                        )
                );

        question.setActiveRow(false);

        questionRepository.save(question);

        return "Question deleted successfully.";
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private QuestionResponseDTO convertToResponse(
            Question question,
            List<QuestionAttribute> attributes) {

        QuestionResponseDTO response =
                new QuestionResponseDTO();

        response.setQuestionId(
                question.getQuestionId()
        );

        response.setQuestionText(
                question.getQuestionText()
        );


        // Course
        response.setCourseId(
                question.getCourse().getCourseId()
        );

        response.setCourseName(
                question.getCourse().getName()
        );


        // Chapter
        response.setChapterId(
                question.getChapter().getChapterId()
        );

        response.setChapterName(
                question.getChapter().getName()
        );


        // Category
        response.setCategoryId(
                question
                        .getQuestionCategory()
                        .getCategoryId()
        );

        response.setCategoryName(
                question
                        .getQuestionCategory()
                        .getName()
        );


        response.setActiveRow(
                question.getActiveRow()
        );

        response.setCreatedAt(
                question.getCreatedAt()
        );

        response.setUpdatedAt(
                question.getUpdatedAt()
        );


        // Question Attributes
        List<QuestionAttributeResponseDTO>
                attributeResponses =
                new ArrayList<>();

        for (QuestionAttribute questionAttribute
                : attributes) {

            QuestionAttributeResponseDTO attributeResponse =
                    new QuestionAttributeResponseDTO();


            attributeResponse.setQuestionAttributeId(
                    questionAttribute
                            .getQuestionAttributeId()
            );


            // Header
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


            // Attribute
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
}