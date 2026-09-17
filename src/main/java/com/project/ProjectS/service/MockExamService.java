package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mock-exam module. Mirrors {@link ExamService} but for practice papers:
 * a mock exam is scoped only to a course + chapters (no college / branch /
 * section, no start / end window). The listing is deliberately role-agnostic
 * - every authenticated user sees every mock exam.
 */
@Transactional
@Service
public class MockExamService {

    private final MockExamRepository mockExamRepository;
    private final MockExamQuestionRepository mockExamQuestionRepository;
    private final MockExamResultRepository mockExamResultRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;
    private final UserRepository userRepository;
    private final SubscriptionEntitlementService entitlementService;
    private final ExamScoringService examScoringService;

    public MockExamService(
            MockExamRepository mockExamRepository,
            MockExamQuestionRepository mockExamQuestionRepository,
            MockExamResultRepository mockExamResultRepository,
            CourseRepository courseRepository,
            ChapterRepository chapterRepository,
            QuestionRepository questionRepository,
            QuestionService questionService,
            UserRepository userRepository,
            SubscriptionEntitlementService entitlementService,
            ExamScoringService examScoringService) {

        this.mockExamRepository = mockExamRepository;
        this.mockExamQuestionRepository = mockExamQuestionRepository;
        this.mockExamResultRepository = mockExamResultRepository;
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.questionRepository = questionRepository;
        this.questionService = questionService;
        this.userRepository = userRepository;
        this.entitlementService = entitlementService;
        this.examScoringService = examScoringService;
    }


    @Transactional
    public MockExamResponseDTO createMockExam(MockExamRequestDTO request) {

        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found"));

        List<Chapter> chapters =
                chapterRepository.findAllById(request.getChapterIds());

        if (chapters.size() != request.getChapterIds().size()) {
            throw new RuntimeException("One or more chapters not found");
        }

        MockExam mockExam = new MockExam();

        mockExam.setMockExamName(request.getMockExamName());
        mockExam.setCourse(course);
        mockExam.setChapters(chapters);
        mockExam.setPassPercentage(request.getPassPercentage());
        mockExam.setActiveRow(true);
        mockExam.setRowStatus(1);

        return convertToResponse(mockExamRepository.save(mockExam));
    }


    /**
     * Every mock exam, for every authenticated caller - no role scoping.
     */
    public List<MockExamResponseDTO> getAllMockExams() {

        return mockExamRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    public MockExamResponseDTO getMockExamById(Long mockExamId) {

        MockExam mockExam = mockExamRepository
                .findById(mockExamId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock exam not found with id: " + mockExamId));

        return convertToResponse(mockExam);
    }


    public MockExamResponseDTO updateMockExam(
            Long mockExamId,
            MockExamRequestDTO request) {

        MockExam mockExam = mockExamRepository
                .findById(mockExamId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock exam not found with id: " + mockExamId));

        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found"));

        List<Chapter> chapters =
                chapterRepository.findAllById(request.getChapterIds());

        if (chapters.size() != request.getChapterIds().size()) {
            throw new RuntimeException("One or more chapters not found");
        }

        mockExam.setMockExamName(request.getMockExamName());
        mockExam.setCourse(course);
        mockExam.setChapters(chapters);
        mockExam.setPassPercentage(request.getPassPercentage());

        return convertToResponse(mockExamRepository.save(mockExam));
    }


    @Transactional
    public void deleteMockExam(Long mockExamId) {

        if (!mockExamRepository.existsById(mockExamId)) {
            throw new RuntimeException(
                    "Mock exam not found with id: " + mockExamId);
        }

        mockExamRepository.deleteById(mockExamId);
    }


    @Transactional
    public void addQuestionsToMockExam(
            Long mockExamId,
            AddMockExamQuestionsRequestDTO request) {

        MockExam mockExam = mockExamRepository.findById(mockExamId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock exam not found with id: " + mockExamId));

        List<Long> mockExamChapterIds = mockExam.getChapters()
                .stream()
                .map(Chapter::getChapterId)
                .toList();

        List<Question> availableQuestions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
                                mockExam.getCourse().getCourseId(),
                                mockExamChapterIds
                        );

        List<Long> availableQuestionIds =
                availableQuestions.stream()
                        .map(Question::getQuestionId)
                        .toList();

        for (Long questionId : request.getQuestionIds()) {

            if (!availableQuestionIds.contains(questionId)) {
                throw new RuntimeException(
                        "Question " + questionId +
                                " does not belong to the selected chapters of this mock exam"
                );
            }

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Question not found with id: " + questionId));

            boolean alreadyExists =
                    mockExamQuestionRepository
                            .existsByMockExam_MockExamIdAndQuestion_QuestionId(
                                    mockExamId,
                                    questionId
                            );

            if (alreadyExists) {
                continue;
            }

            MockExamQuestion mockExamQuestion = new MockExamQuestion();

            mockExamQuestion.setMockExam(mockExam);
            mockExamQuestion.setQuestion(question);

            mockExamQuestionRepository.save(mockExamQuestion);
        }
    }


    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getMockExamQuestions(Long mockExamId) {

        if (!mockExamRepository.existsById(mockExamId)) {
            throw new RuntimeException(
                    "Mock exam not found with id: " + mockExamId);
        }

        List<Long> questionIds =
                mockExamQuestionRepository
                        .findByMockExam_MockExamId(mockExamId)
                        .stream()
                        .map(mockExamQuestion ->
                                mockExamQuestion.getQuestion().getQuestionId())
                        .toList();

        return questionService.getQuestionsByIds(questionIds);
    }


    public List<QuestionResponseDTO> getMockExamQuestions(
            Long mockExamId,
            Authentication authentication) {

        MockExam mockExam = mockExamRepository.findById(mockExamId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock exam not found with id: " + mockExamId));

        if (authentication != null && isStudent(authentication)) {
//            entitlementService.requireCourseAccess(
//                    authentication.getName(),
//                    mockExam.getCourse().getCourseId());
        }

        return getMockExamQuestions(mockExamId);
    }


    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getAvailableMockExamQuestions(Long mockExamId) {

        MockExam mockExam = mockExamRepository.findById(mockExamId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock exam not found with id: " + mockExamId));

        List<Long> mockExamChapterIds = mockExam.getChapters()
                .stream()
                .map(Chapter::getChapterId)
                .toList();

        List<Question> availableQuestions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
                                mockExam.getCourse().getCourseId(),
                                mockExamChapterIds
                        );

        List<Long> addedQuestionIds =
                mockExamQuestionRepository.findByMockExam_MockExamId(mockExamId)
                        .stream()
                        .map(mockExamQuestion ->
                                mockExamQuestion.getQuestion().getQuestionId())
                        .toList();

        List<Long> availableQuestionIds =
                availableQuestions.stream()
                        .map(Question::getQuestionId)
                        .filter(questionId -> !addedQuestionIds.contains(questionId))
                        .toList();

        return questionService.getQuestionsByIds(availableQuestionIds);
    }


    @Transactional
    public void removeQuestionFromMockExam(
            Long mockExamId,
            Long questionId) {

        if (!mockExamRepository.existsById(mockExamId)) {
            throw new RuntimeException(
                    "Mock exam not found with id: " + mockExamId);
        }

        boolean exists =
                mockExamQuestionRepository
                        .existsByMockExam_MockExamIdAndQuestion_QuestionId(
                                mockExamId,
                                questionId
                        );

        if (!exists) {
            throw new RuntimeException(
                    "Question is not added to this mock exam");
        }

        mockExamQuestionRepository
                .deleteByMockExam_MockExamIdAndQuestion_QuestionId(
                        mockExamId,
                        questionId
                );
    }


    public MockExamSubmitResponseDTO submitMockExam(
            Long mockExamId,
            MockExamSubmitRequestDTO request) {

        MockExam mockExam = mockExamRepository.findById(mockExamId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Mock exam not found with id: " + mockExamId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + request.getUserId()));

        List<Long> questionIds =
                mockExamQuestionRepository.findByMockExam_MockExamId(mockExamId)
                        .stream()
                        .map(mockExamQuestion ->
                                mockExamQuestion.getQuestion().getQuestionId())
                        .toList();

        ExamScoringService.Score score =
                examScoringService.score(questionIds, request.getAnswers());

        MockExamResult result = new MockExamResult();

        result.setMockExam(mockExam);
        result.setUser(user);
        result.setTotalMarks(score.totalMarks());
        result.setPercentage(score.percentage());
        result.setMaximumMarks(score.maximumMarks());
        result.setTimeTakenSeconds(request.getTimeTakenSeconds());

        mockExamResultRepository.save(result);

        examScoringService.persistAnswerInfo(
                user, null, mockExam, request.getAnswers(), score.questionScores());

        MockExamSubmitResponseDTO response =
                new MockExamSubmitResponseDTO();

        response.setMockExamResultId(result.getMockExamResultId());
        response.setMockExamId(mockExamId);
        response.setUserId(user.getUserId());
        response.setTotalMarks(score.totalMarks());
        response.setPercentage(score.percentage());

        return response;
    }

    /**
     * The "Exam Review" screen's payload for one mock exam attempt - Journal,
     * Dropdown, Drag-and-drop and MCQ answers are all rehydrated from this
     * mock exam's own AnswerEvent rows (tagged by mock_exam_id at submit
     * time), so retaking this mock exam or another one sharing a question
     * never mixes attempts together. Any authenticated user may view their
     * own result; SUPER_ADMIN/COLLEGE_ADMIN/BRANCH_ADMIN may view any.
     */
    public ExamReviewResponseDTO getMockExamResultReview(
            Long mockExamId, Long resultId, Authentication authentication) {

        MockExamResult result = mockExamResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException(
                        "Mock exam result not found with id: " + resultId));

        if (!result.getMockExam().getMockExamId().equals(mockExamId)) {
            throw new RuntimeException(
                    "Mock exam result " + resultId
                            + " does not belong to mock exam " + mockExamId);
        }

        User caller = getLoggedInUser(authentication);
        boolean isOwner = caller != null
                && caller.getUserId().equals(result.getUser().getUserId());
        boolean isManager = caller != null
                && caller.getRole() != null
                && List.of("SUPER_ADMIN", "COLLEGE_ADMIN", "BRANCH_ADMIN")
                        .contains(caller.getRole().getRoleName().toUpperCase());

        if (!isOwner && !isManager) {
            throw new RuntimeException("Not authorised to view this result");
        }

        ExamReviewResponseDTO response = new ExamReviewResponseDTO();
        response.setResultId(result.getMockExamResultId());
        response.setExamId(result.getMockExam().getMockExamId());
        response.setExamName(result.getMockExam().getMockExamName());
        response.setCourseName(result.getMockExam().getCourse().getName());
        response.setTotalMarks(result.getTotalMarks());
        response.setPercentage(result.getPercentage());
        response.setMaximumMarks(result.getMaximumMarks());
        response.setTimeTakenSeconds(result.getTimeTakenSeconds());
        response.setCompletedAt(result.getCreatedAt());

        response.setQuestions(
                examScoringService.buildReviewFromAnswerInfo(
                        result.getUser().getUserId(), null, mockExamId));

        return response;
    }

    /**
     * Every completed mock-exam attempt for the given user, newest first -
     * feeds the dashboard's Recent Activity list.
     */
    public List<ExamAttemptSummaryDTO> getMyMockExamAttempts(Authentication authentication) {

        User user = getLoggedInUser(authentication);
        if (user == null) {
            return List.of();
        }

        return mockExamResultRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(result -> {
                    ExamAttemptSummaryDTO dto = new ExamAttemptSummaryDTO();
                    dto.setResultId(result.getMockExamResultId());
                    dto.setExamId(result.getMockExam().getMockExamId());
                    dto.setExamName(result.getMockExam().getMockExamName());
                    dto.setExamType("MOCK_EXAM");
                    dto.setPercentage(result.getPercentage());
                    dto.setCompletedAt(result.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    private User getLoggedInUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName())
                .orElse(null);
    }


    public MockExamSubmitResponseDTO submitMockExam(
            Long mockExamId,
            MockExamSubmitRequestDTO request,
            Authentication authentication) {

        if (authentication != null) {
            request.setUserId(userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() ->
                            new RuntimeException("Authenticated user not found"))
                    .getUserId());
        }

        return submitMockExam(mockExamId, request);
    }


    private boolean isStudent(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority())
                        || "ROLE_GUEST".equals(a.getAuthority()));
    }


    private MockExamResponseDTO convertToResponse(MockExam mockExam) {

        MockExamResponseDTO response = new MockExamResponseDTO();

        response.setMockExamId(mockExam.getMockExamId());
        response.setMockExamName(mockExam.getMockExamName());
        response.setCourseId(mockExam.getCourse().getCourseId());
        response.setCourseName(mockExam.getCourse().getName());
        response.setChapterIds(
                mockExam.getChapters()
                        .stream()
                        .map(Chapter::getChapterId)
                        .toList()
        );
        response.setChapterNames(
                mockExam.getChapters()
                        .stream()
                        .map(Chapter::getName)
                        .toList()
        );
        response.setPassPercentage(mockExam.getPassPercentage());
        response.setActiveRow(mockExam.getActiveRow());
        response.setRowStatus(mockExam.getRowStatus());
        response.setCreatedAt(mockExam.getCreatedAt());
        response.setUpdatedAt(mockExam.getUpdatedAt());

        return response;
    }
}
