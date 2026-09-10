package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import org.springframework.security.core.Authentication;
import com.project.ProjectS.security.service.CustomUserDetails;

@Transactional
@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionService questionService;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamResultRepository examResultRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final UserRepository userRepository;
    private final SubscriptionEntitlementService entitlementService;
    private final ExamScoringService examScoringService;

    public ExamService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamResultRepository examResultRepository,
            CollegeRepository collegeRepository,
            BranchRepository branchRepository,
            CourseRepository courseRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            ChapterRepository chapterRepository,
            QuestionRepository questionRepository,
            QuestionService questionService,
            McqQuestionRepository mcqQuestionRepository,
            UserRepository userRepository,
            SubscriptionEntitlementService entitlementService,
            ExamScoringService examScoringService) {

        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.chapterRepository = chapterRepository;
        this.questionRepository = questionRepository;
        this.questionService = questionService;
        this.examResultRepository = examResultRepository;
        this.mcqQuestionRepository = mcqQuestionRepository;
        this.userRepository = userRepository;
        this.entitlementService = entitlementService;
        this.examScoringService = examScoringService;
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


        Subject subject = resolveSubjectForExam(
                request.getSubjectId(), course, chapters
        );


        // Create Exam
        Exam exam = new Exam();

        exam.setExamName(request.getExamName());

        exam.setCollege(college);

        exam.setBranch(branch);

        exam.setCourse(course);

        exam.setSection(section);

        exam.setSubject(subject);

        exam.setChapters(chapters);

        exam.setStartDate(request.getStartDate());

        exam.setEndDate(request.getEndDate());

        exam.setPassPercentage(request.getPassPercentage());

        exam.setActiveRow(true);

        exam.setRowStatus(1);


        Exam savedExam =
                examRepository.save(exam);


        return convertToResponse(savedExam);
    }


    /**
     * Returns exams scoped to the caller's role:
     * SUPER_ADMIN   -> every exam on the platform
     * COLLEGE_ADMIN -> exams belonging to the admin's college
     * BRANCH_ADMIN  -> exams belonging to the admin's branch
     * STUDENT       -> exams for the student's own section
     * GUEST / anyone else / unauthenticated -> none
     * The scope is applied by the repository query, not in memory.
     */
    public List<ExamResponseDTO> getAllExams(Authentication authentication) {

        User user = getLoggedInUser(authentication);

        if (user == null || user.getRole() == null) {
            return List.of();
        }

        String role = user.getRole().getRoleName();

        List<Exam> exams = switch (role == null ? "" : role.toUpperCase()) {

            case "SUPER_ADMIN" -> examRepository.findAll();

            case "COLLEGE_ADMIN" -> user.getCollege() == null
                    ? List.of()
                    : examRepository.findByCollege_CollegeId(
                    user.getCollege().getCollegeId());

            case "BRANCH_ADMIN" -> user.getBranch() == null
                    ? List.of()
                    : examRepository.findByBranch_BranchId(
                    user.getBranch().getBranchId());

            case "STUDENT" -> user.getSection() == null
                    ? List.of()
                    : examRepository.findUnattemptedForStudent(
                    user.getSection().getSectionId(),
                    user.getUserId());

            default -> List.of();
        };

        return exams.stream()
                .map(this::convertToResponse)
                .toList();
    }


    private User getLoggedInUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return null;
        }

        // Re-load a managed entity by primary key: the User held by the
        // principal was loaded outside a transaction, so its lazy
        // college/branch/section associations are not usable here.
        return userRepository
                .findById(principal.getUser().getUserId())
                .orElse(null);
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


        Subject subject = resolveSubjectForExam(
                request.getSubjectId(), course, chapters
        );


        exam.setExamName(request.getExamName());

        exam.setCollege(college);

        exam.setBranch(branch);

        exam.setCourse(course);

        exam.setSection(section);

        exam.setSubject(subject);

        exam.setChapters(chapters);

        exam.setStartDate(request.getStartDate());

        exam.setEndDate(request.getEndDate());

        exam.setPassPercentage(request.getPassPercentage());


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

    public ExamSubmitResponseDTO submitExam(
            Long examId,
            ExamSubmitRequestDTO request) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Exam not found with id: " + examId)
                );

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + request.getUserId())
                );

        entitlementService.consumeExamAttempt(
                user.getUserId(), exam.getCourse().getCourseId());

        List<Long> questionIds =
                examQuestionRepository.findByExam_ExamId(examId)
                        .stream()
                        .map(examQuestion ->
                                examQuestion.getQuestion().getQuestionId())
                        .toList();

        ExamScoringService.Score score =
                examScoringService.score(questionIds, request.getAnswers());

        ExamResult result = new ExamResult();

        result.setExam(exam);
        result.setUser(user);
        result.setTotalMarks(score.totalMarks());
        result.setPercentage(score.percentage());

        examResultRepository.save(result);

        ExamSubmitResponseDTO response =
                new ExamSubmitResponseDTO();

        response.setExamId(examId);
        response.setUserId(user.getUserId());
        response.setTotalMarks(score.totalMarks());
        response.setPercentage(score.percentage());

        return response;
    }

    public ExamSubmitResponseDTO submitExam(Long examId, ExamSubmitRequestDTO request,
                                            Authentication authentication) {
        if (authentication != null) {
            request.setUserId(userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                    .getUserId());
        }
        return submitExam(examId, request);
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

    public List<QuestionResponseDTO> getExamQuestions(Long examId, Authentication authentication) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
        if (authentication != null && isStudent(authentication)) {
            entitlementService.requireCourseAccess(authentication.getName(),
                    exam.getCourse().getCourseId());
        }
        return getExamQuestions(examId);
    }

    private boolean isStudent(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority())
                        || "ROLE_GUEST".equals(a.getAuthority()));
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


    // An exam is scoped to a single subject: it must sit under the exam's
    // course, and every chapter attached to the paper must belong to it.
    private Subject resolveSubjectForExam(
            Long subjectId,
            Course course,
            List<Chapter> chapters) {

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (!subject.getCourse().getCourseId()
                .equals(course.getCourseId())) {
            throw new RuntimeException(
                    "Subject does not belong to the selected course"
            );
        }

        boolean allChaptersInSubject = chapters.stream()
                .allMatch(chapter ->
                        chapter.getSubject().getSubjectId()
                                .equals(subject.getSubjectId()));

        if (!allChaptersInSubject) {
            throw new RuntimeException(
                    "One or more chapters do not belong to the selected subject"
            );
        }

        return subject;
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

        response.setSubjectId(
                exam.getSubject()
                        .getSubjectId()
        );

        response.setSubjectName(
                exam.getSubject()
                        .getSubjectName()
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

        response.setPassPercentage(
                exam.getPassPercentage()
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
