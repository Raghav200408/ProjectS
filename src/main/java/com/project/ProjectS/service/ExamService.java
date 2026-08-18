package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.AddExamQuestionsRequestDTO;
import com.project.ProjectS.model.ExamRequestDTO;
import com.project.ProjectS.model.ExamResponseDTO;
import com.project.ProjectS.model.QuestionResponseDTO;
import com.project.ProjectS.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionService questionService;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionAttributeRepository questionAttributeRepository;

    public ExamService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            CollegeRepository collegeRepository,
            BranchRepository branchRepository,
            CourseRepository courseRepository,
            SectionRepository sectionRepository,
            ChapterRepository chapterRepository,
            QuestionRepository questionRepository,
            QuestionAttributeRepository questionAttributeRepository,
            QuestionService questionService) {

        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.chapterRepository = chapterRepository;
        this.questionRepository = questionRepository;
        this.questionAttributeRepository = questionAttributeRepository;
        this.questionService = questionService;
    }


    @Transactional
    public ExamResponseDTO createExam(ExamRequestDTO request) {

        College college = collegeRepository
                .findById(request.getCollegeId())
                .orElseThrow(() ->
                        new RuntimeException("College not found"));

        Branch branch = branchRepository
                .findById(request.getBranchId())
                .orElseThrow(() ->
                        new RuntimeException("Branch not found"));

        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found"));

        Section section = sectionRepository
                .findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException("Section not found"));


        List<Chapter> chapters =
                chapterRepository.findAllById(
                        request.getChapterIds()
                );


        if (chapters.size() != request.getChapterIds().size()) {
            throw new RuntimeException(
                    "One or more chapters not found"
            );
        }


        // Create Exam
        Exam exam = new Exam();

        exam.setExamName(request.getExamName());

        exam.setCollege(college);

        exam.setBranch(branch);

        exam.setCourse(course);

        exam.setSection(section);

        exam.setChapters(chapters);

        exam.setStartDate(request.getStartDate());

        exam.setEndDate(request.getEndDate());

        exam.setActiveRow(true);

        exam.setRowStatus(1);


        Exam savedExam =
                examRepository.save(exam);


        return convertToResponse(savedExam);
    }


    public List<ExamResponseDTO> getAllExams() {

        return examRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    public ExamResponseDTO getExamById(Long examId) {

        Exam exam = examRepository
                .findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: "
                                        + examId
                        ));

        return convertToResponse(exam);
    }


    public ExamResponseDTO updateExam(
            Long examId,
            ExamRequestDTO request) {

        Exam exam = examRepository
                .findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: "
                                        + examId
                        ));


        College college = collegeRepository
                .findById(request.getCollegeId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "College not found"
                        ));


        Branch branch = branchRepository
                .findById(request.getBranchId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Branch not found"
                        ));


        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found"
                        ));


        Section section = sectionRepository
                .findById(request.getSectionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Section not found"
                        ));


        List<Chapter> chapters =
                chapterRepository.findAllById(
                        request.getChapterIds()
                );


        if (chapters.size() != request.getChapterIds().size()) {
            throw new RuntimeException(
                    "One or more chapters not found"
            );
        }


        exam.setExamName(request.getExamName());

        exam.setCollege(college);

        exam.setBranch(branch);

        exam.setCourse(course);

        exam.setSection(section);

        exam.setChapters(chapters);

        exam.setStartDate(request.getStartDate());

        exam.setEndDate(request.getEndDate());


        Exam updatedExam =
                examRepository.save(exam);


        return convertToResponse(updatedExam);
    }


    @Transactional
    public void deleteExam(Long examId) {

        if (!examRepository.existsById(examId)) {

            throw new RuntimeException(
                    "Exam not found with id: "
                            + examId
            );
        }

        examRepository.deleteById(examId);
    }


    @Transactional
    public void addQuestionsToExam(
            Long examId,
            AddExamQuestionsRequestDTO request) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));


        List<Long> examChapterIds = exam.getChapters()
                .stream()
                .map(Chapter::getChapterId)
                .toList();


        List<Question> availableQuestions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
                                exam.getCourse().getCourseId(),
                                examChapterIds
                        );

        List<Long> availableQuestionIds =
                availableQuestions.stream()
                        .map(Question::getQuestionId)
                        .toList();

        for (Long questionId : request.getQuestionIds()) {

            // Make sure question belongs to exam's course + chapters
            if (!availableQuestionIds.contains(questionId)) {
                throw new RuntimeException(
                        "Question " + questionId +
                                " does not belong to the selected chapters of this exam"
                );
            }

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Question not found with id: " + questionId
                            ));

            // Prevent duplicate question
            boolean alreadyExists =
                    examQuestionRepository
                            .existsByExam_ExamIdAndQuestion_QuestionId(
                                    examId,
                                    questionId
                            );

            if (alreadyExists) {
                continue;
            }

            ExamQuestion examQuestion = new ExamQuestion();

            examQuestion.setExam(exam);
            examQuestion.setQuestion(question);

            examQuestionRepository.save(examQuestion);
        }
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getAvailableQuestions(Long examId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exam not found with id: " + examId
                        ));


        List<Long> examChapterIds = exam.getChapters()
                .stream()
                .map(Chapter::getChapterId)
                .toList();


        List<Question> availableQuestions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
                                exam.getCourse().getCourseId(),
                                examChapterIds
                        );


        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExam_ExamId(examId);

        List<Long> addedQuestionIds =
                examQuestions.stream()
                        .map(examQuestion ->
                                examQuestion.getQuestion().getQuestionId()
                        )
                        .toList();


        List<Long> availableQuestionIds =
                availableQuestions.stream()
                        .map(Question::getQuestionId)
                        .filter(questionId -> !addedQuestionIds.contains(questionId))
                        .toList();


        return questionService.getQuestionsByIds(availableQuestionIds);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getExamQuestions(Long examId) {

        if (!examRepository.existsById(examId)) {
            throw new RuntimeException(
                    "Exam not found with id: " + examId
            );
        }

        List<ExamQuestion> examQuestions =
                examQuestionRepository
                        .findByExam_ExamId(examId);

        List<Long> questionIds =
                examQuestions.stream()
                        .map(examQuestion ->
                                examQuestion
                                        .getQuestion()
                                        .getQuestionId()
                        )
                        .toList();

        return questionService.getQuestionsByIds(questionIds);
    }


    @Transactional
    public void removeQuestionFromExam(
            Long examId,
            Long questionId) {

        if (!examRepository.existsById(examId)) {
            throw new RuntimeException(
                    "Exam not found with id: " + examId
            );
        }

        boolean exists =
                examQuestionRepository
                        .existsByExam_ExamIdAndQuestion_QuestionId(
                                examId,
                                questionId
                        );

        if (!exists) {
            throw new RuntimeException(
                    "Question is not added to this exam"
            );
        }

        examQuestionRepository
                .deleteByExam_ExamIdAndQuestion_QuestionId(
                        examId,
                        questionId
                );
    }


    private ExamResponseDTO convertToResponse(
            Exam exam) {

        ExamResponseDTO response =
                new ExamResponseDTO();


        response.setExamId(
                exam.getExamId()
        );


        response.setExamName(
                exam.getExamName()
        );


        response.setCollegeId(
                exam.getCollege()
                        .getCollegeId()
        );


        response.setBranchId(
                exam.getBranch()
                        .getBranchId()
        );

        response.setCourseId(
                exam.getCourse()
                        .getCourseId()
        );

        response.setCourseName(
                exam.getCourse()
                        .getName()
        );

        response.setSectionId(
                exam.getSection()
                        .getSectionId()
        );


        response.setChapterIds(
                exam.getChapters()
                        .stream()
                        .map(Chapter::getChapterId)
                        .toList()
        );


        response.setStartDate(
                exam.getStartDate()
        );


        response.setEndDate(
                exam.getEndDate()
        );


        response.setActiveRow(
                exam.getActiveRow()
        );


        response.setRowStatus(
                exam.getRowStatus()
        );


        response.setCreatedAt(
                exam.getCreatedAt()
        );


        response.setUpdatedAt(
                exam.getUpdatedAt()
        );


        return response;
    }
}