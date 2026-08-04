package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionAttributeRepository questionAttributeRepository;


    // =========================================================
    // CREATE EXAM
    // =========================================================

    public ExamResponseDTO createExam(
            ExamRequestDTO request) {

        College college =
                getCollege(request.getCollegeId());

        Branch branch =
                getBranch(request.getBranchId());

        Course course =
                getCourse(request.getCourseId());

        Section section =
                getSection(request.getSectionId());

        Exam exam = new Exam();

        exam.setExamName(
                request.getExamName()
        );

        exam.setCollege(college);

        exam.setBranch(branch);

        exam.setCourse(course);

        exam.setSection(section);

        exam.setStartDate(
                request.getStartDate()
        );

        exam.setEndDate(
                request.getEndDate()
        );

        exam.setActiveRow(true);

        exam.setRowStatus(1);

        Exam savedExam =
                examRepository.save(exam);

        for (Long questionId : request.getQuestionIds()) {

            Question question =
                    getQuestion(questionId);

            ExamQuestion examQuestion =
                    new ExamQuestion();

            examQuestion.setExam(savedExam);

            examQuestion.setQuestion(question);

            examQuestionRepository.save(
                    examQuestion
            );
        }

        return convertToResponse(savedExam);
    }


    // =========================================================
    // GET ALL EXAMS
    // =========================================================

    public List<ExamResponseDTO> getAllExams() {

        List<Exam> exams = examRepository.findByActiveRowTrue();
        return exams.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // =========================================================
    // GET EXAM BY ID
    // =========================================================

    public ExamResponseDTO getExamById(
            Long examId) {

        Exam exam =
                examRepository.findById(examId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Exam not found with id : "
                                                + examId
                                )
                        );

        return convertToResponse(exam);
    }

    private College getCollege(Long collegeId) {

        return collegeRepository.findById(collegeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "College not found with id: "
                                        + collegeId
                        )
                );
    }

    private Branch getBranch(Long branchId) {

        return branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Branch not found with id: "
                                        + branchId
                        )
                );
    }

    private Course getCourse(Long courseId) {

        return courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found with id: "
                                        + courseId
                        )
                );
    }

    private Section getSection(Long sectionId) {

        return sectionRepository.findById(sectionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Section not found with id: "
                                        + sectionId
                        )
                );
    }

    private Question getQuestion(Long questionId) {

        return questionRepository.findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: "
                                        + questionId
                        )
                );
    }

    public ExamResponseDTO updateExam(
            Long examId,
            ExamRequestDTO request) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));

        exam.setExamName(request.getExamName());

        exam.setCollege(
                getCollege(request.getCollegeId())
        );

        exam.setBranch(
                getBranch(request.getBranchId())
        );

        exam.setCourse(
                getCourse(request.getCourseId())
        );

        exam.setSection(
                getSection(request.getSectionId())
        );

        exam.setStartDate(
                request.getStartDate()
        );

        exam.setEndDate(
                request.getEndDate()
        );

        Exam updatedExam = examRepository.save(exam);

        examQuestionRepository.deleteByExam_ExamId(
                updatedExam.getExamId()
        );

// Force DELETE to execute immediately
        examQuestionRepository.flush();

        for (Long questionId : request.getQuestionIds()) {

            ExamQuestion examQuestion = new ExamQuestion();

            examQuestion.setExam(updatedExam);

            examQuestion.setQuestion(
                    getQuestion(questionId)
            );

            examQuestionRepository.save(examQuestion);
        }

        return convertToResponse(updatedExam);
    }

    public String deleteExam(Long examId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));

        exam.setActiveRow(false);

        examRepository.save(exam);

        return "Exam deleted successfully.";
    }
    public String restoreExam(Long examId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));

        exam.setActiveRow(true);

        examRepository.save(exam);

        return "Exam restored successfully.";
    }

    private ExamResponseDTO convertToResponse(Exam exam) {

        ExamResponseDTO dto = new ExamResponseDTO();

        dto.setExamId(exam.getExamId());

        dto.setExamName(exam.getExamName());

        // College
        if (exam.getCollege() != null) {

            dto.setCollegeId(
                    exam.getCollege().getCollegeId()
            );

            dto.setCollegeName(
                    exam.getCollege().getInstituteName()
            );
        }

        // Branch
        if (exam.getBranch() != null) {

            dto.setBranchId(
                    exam.getBranch().getBranchId()
            );

            dto.setBranchName(
                    exam.getBranch().getBranchName()
            );
        }

        // Course
        if (exam.getCourse() != null) {

            dto.setCourseId(
                    exam.getCourse().getCourseId()
            );

            dto.setCourseName(
                    exam.getCourse().getName()
            );
        }

        // Section
        if (exam.getSection() != null) {

            dto.setSectionId(
                    exam.getSection().getSectionId()
            );

            dto.setSectionName(
                    exam.getSection().getSectionName()
            );
        }

        dto.setStartDate(exam.getStartDate());

        dto.setEndDate(exam.getEndDate());

        dto.setActiveRow(exam.getActiveRow());

        dto.setRowStatus(exam.getRowStatus());

        dto.setCreatedAt(exam.getCreatedAt());

        dto.setUpdatedAt(exam.getUpdatedAt());

        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExam_ExamId(
                        exam.getExamId()
                );

        List<QuestionResponseDTO> questionResponses =
                new ArrayList<>();

        for (ExamQuestion examQuestion : examQuestions) {

            Question question = examQuestion.getQuestion();

            QuestionResponseDTO questionResponse =
                    convertQuestionToResponse(question);

            questionResponses.add(questionResponse);
        }

        dto.setQuestions(questionResponses);
        return dto;
    }

    private QuestionResponseDTO convertQuestionToResponse(
            Question question) {

        List<QuestionAttribute> attributes =
                questionAttributeRepository
                        .findByQuestion_QuestionId(
                                question.getQuestionId()
                        );

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
                question.getQuestionCategory().getCategoryId()
        );

        response.setCategoryName(
                question.getQuestionCategory().getName()
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

        List<QuestionAttributeResponseDTO> attributeResponses =
                new ArrayList<>();

        for (QuestionAttribute attribute : attributes) {

            QuestionAttributeResponseDTO dto =
                    new QuestionAttributeResponseDTO();

            dto.setQuestionAttributeId(
                    attribute.getQuestionAttributeId()
            );

            if (attribute.getHeader() != null) {

                dto.setHeaderId(
                        attribute.getHeader().getHeaderId()
                );

                dto.setHeaderName(
                        attribute.getHeader().getName()
                );
            }

            if (attribute.getAttribute() != null) {

                dto.setAttributeId(
                        attribute.getAttribute().getAttributeId()
                );

                dto.setAttributeName(
                        attribute.getAttribute().getName()
                );
            }

            dto.setTransactionDate(
                    attribute.getTransactionDate()
            );

            dto.setAmount(
                    attribute.getAmount()
            );

            dto.setAmount2(
                    attribute.getAmount2()
            );

            dto.setNote(
                    attribute.getNote()
            );

            dto.setActiveRow(
                    attribute.getActiveRow()
            );

            attributeResponses.add(dto);
        }

        response.setQuestionAttributes(attributeResponses);

        return response;
    }
}