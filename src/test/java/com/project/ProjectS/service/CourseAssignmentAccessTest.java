package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseAssignmentAccessTest {
    private final CourseAssignmentRepository mappings = mock(CourseAssignmentRepository.class);
    private final CourseRepository courses = mock(CourseRepository.class);
    private final BranchRepository branches = mock(BranchRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UserSubscriptionRepository subscriptions = mock(UserSubscriptionRepository.class);
    private final PlanCourseRepository plans = mock(PlanCourseRepository.class);
    private final PlanQuestionRepository selected = mock(PlanQuestionRepository.class);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final SubscriptionQuestionUsageRepository usage = mock(SubscriptionQuestionUsageRepository.class);
    private final CourseAssignmentService assignments = new CourseAssignmentService(mappings, courses, branches, users);
    private final SubscriptionEntitlementService entitlements = new SubscriptionEntitlementService(subscriptions, users, plans, selected, questions);
    private final LearningAccessService access = new LearningAccessService(assignments, entitlements, users, questions, usage);
    private final Course course = course();
    private final Branch branchA = branch(10, 1), branchB = branch(20, 2), branchC = branch(30, 2);
    private final User studentA = user(1, "STUDENT", branchA), studentB = user(2, "STUDENT", branchB);
    private final Map<Long, UserSubscription> personal = new HashMap<>();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(entitlements, "assignments", assignments);
        when(courses.findById(101L)).thenReturn(Optional.of(course));
        for (Branch branch : List.of(branchA, branchB, branchC)) when(branches.findById(branch.getBranchId())).thenReturn(Optional.of(branch));
        for (User user : List.of(studentA, studentB)) {
            when(users.findById(user.getUserId())).thenReturn(Optional.of(user));
            when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            personal.put(user.getUserId(), subscription(user));
        }
        when(mappings.existsByCourse_CourseIdAndBranch_BranchIdAndActiveRowTrue(101L, 10L)).thenReturn(true);
        when(mappings.existsByCourse_CourseIdAndBranch_BranchIdAndActiveRowTrue(101L, 20L)).thenReturn(true);
        when(subscriptions.findEntitledSubscription(anyLong(), eq(101L), any())).thenAnswer(call -> Optional.ofNullable(personal.get(call.getArgument(0))));
        PlanCourse plan = new PlanCourse(); plan.setPlanCourseId(5L); plan.setCourse(course); plan.setAllCourseQuestions(true);
        when(plans.findByPlan_PlanIdAndCourse_CourseId(1L, 101L)).thenReturn(Optional.of(plan));
        Question question = question(11L); when(questions.findById(11L)).thenReturn(Optional.of(question));
        when(questions.findByCourse_CourseIdAndActiveRowTrue(101L)).thenReturn(List.of(question));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void twoCollegesReuseExactlyTheSameCourseWithDifferentPersonalSubscriptions() {
        assertTrue(assignments.eligible(studentA, course)); assertTrue(assignments.eligible(studentB, course));
        assertSame(course, entitlements.requireCourseAccess(1L, 101L).getCourse());
        assertSame(course, entitlements.requireCourseAccess(2L, 101L).getCourse());
        assertNotEquals(personal.get(1L).getSubscriptionId(), personal.get(2L).getSubscriptionId());
    }
    @Test void unassignedBranchIsRejectedEvenWithAnActivePaidSubscription() {
        studentB.setBranch(branchC);
        assertThrows(AccessDeniedException.class, () -> entitlements.requireCourseAccess(2L, 101L));
    }
    @Test void revokedAssignmentStopsAnExistingSubscription() {
        when(mappings.existsByCourse_CourseIdAndBranch_BranchIdAndActiveRowTrue(101L, 10L)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> entitlements.requireCourseAccess(1L, 101L));
    }
    @Test void noActiveSubscriptionAndDisabledPlanAreRejected() {
        personal.remove(1L);
        assertThrows(ResponseStatusException.class, () -> entitlements.requireCourseAccess(1L, 101L));
        personal.get(2L).getPlan().setActive(false);
        assertThrows(ResponseStatusException.class, () -> entitlements.requireCourseAccess(2L, 101L));
    }
    @Test void questionSelectionIsSharedByPlanWhileAccessRemainsPersonal() {
        PlanCourse plan = new PlanCourse(); plan.setPlanCourseId(5L); plan.setCourse(course); plan.setAllCourseQuestions(false);
        when(plans.findByPlan_PlanIdAndCourse_CourseId(1L, 101L)).thenReturn(Optional.of(plan));
        PlanQuestion row = new PlanQuestion(); row.setQuestion(question(11L));
        when(selected.findByPlanCourse_PlanCourseId(5L)).thenReturn(List.of(row));
        when(questions.findByCourse_CourseIdAndQuestionIdInAndActiveRowTrue(eq(101L), anyList())).thenReturn(List.of(row.getQuestion()));
        assertEquals(Set.of(11L), entitlements.accessibleQuestionIds(studentA.getEmail(), 101L));
        assertEquals(Set.of(11L), entitlements.accessibleQuestionIds(studentB.getEmail(), 101L));
        assertThrows(ResponseStatusException.class, () -> entitlements.requireQuestionAccess(studentA.getEmail(), 101L, 12L));
    }
    @Test void expiredAndFutureSubscriptionsCannotGrantAccessToAnAssignedBranch() {
        personal.get(1L).setExpiresAt(LocalDateTime.now().minusSeconds(1));
        assertThrows(ResponseStatusException.class, () -> entitlements.requireCourseAccess(1L, 101L));
        personal.get(2L).setStartsAt(LocalDateTime.now().plusDays(1));
        assertThrows(ResponseStatusException.class, () -> entitlements.requireCourseAccess(2L, 101L));
    }
    @Test void moduleLimitsAndCountersStaySeparateForEachStudent() {
        entitlements.consumeMockTest(1L, 101L); entitlements.consumeExamAttempt(1L, 101L);
        assertEquals(1, personal.get(1L).getMockTestsUsed()); assertEquals(1, personal.get(1L).getExamAttemptsUsed());
        assertEquals(0, personal.get(2L).getMockTestsUsed()); assertEquals(0, personal.get(2L).getExamAttemptsUsed());
        assertThrows(ResponseStatusException.class, () -> entitlements.consumeMockTest(1L, 101L));
        assertThrows(ResponseStatusException.class, () -> entitlements.consumeExamAttempt(1L, 101L));
        entitlements.consumeMockTest(2L, 101L); assertEquals(1, personal.get(2L).getMockTestsUsed());
    }
    @Test void effectsRetriesAndAnswerEventPairsChargeOneQuestionPerSubscription() {
        Set<String> charged = new HashSet<>();
        when(usage.existsBySubscription_SubscriptionIdAndQuestion_QuestionId(anyLong(), anyLong())).thenAnswer(c -> charged.contains(c.getArgument(0)+":"+c.getArgument(1)));
        when(usage.save(any())).thenAnswer(c -> { SubscriptionQuestionUsage row = c.getArgument(0); charged.add(row.getSubscription().getSubscriptionId()+":"+row.getQuestion().getQuestionId()); return row; });
        access.practice(1L, 11L); access.practice(1L, 11L); access.practice(2L, 11L);
        assertEquals(1, personal.get(1L).getPracticeQuestionsUsed()); assertEquals(1, personal.get(2L).getPracticeQuestionsUsed());
        verify(usage, times(2)).save(any());
    }
    @Test void disabledModuleAndPracticeLimitRejectFurtherConsumption() {
        personal.get(1L).getPlan().setMockTestEnabled(false);
        personal.get(1L).getPlan().setExamEnabled(false);
        assertThrows(ResponseStatusException.class, () -> entitlements.consumeMockTest(1L, 101L));
        assertThrows(ResponseStatusException.class, () -> entitlements.consumeExamAttempt(1L, 101L));
        personal.get(1L).setPracticeQuestionsUsed(1);
        assertThrows(IllegalStateException.class, () -> access.practice(1L, 11L));
        verify(usage, never()).save(any());
    }
    @Test void aScheduledExamCannotBeOpenedFromAnotherBranchEvenWithTheSameCourse() {
        Section section = new Section(); section.setSectionId(50L);
        studentA.setSection(section); studentB.setSection(section);
        Exam exam = new Exam(); exam.setCourse(course); exam.setCollege(branchA.getCollege()); exam.setBranch(branchA); exam.setSection(section);
        assertDoesNotThrow(() -> access.requireExamAudience(studentA, exam));
        assertThrows(AccessDeniedException.class, () -> access.requireExamAudience(studentB, exam));
    }
    @Test void collegeAdministratorsCannotReadAnotherCollegesResults() {
        User adminA = user(8, "COLLEGE_ADMIN", branchA); adminA.setBranch(null);
        assertDoesNotThrow(() -> access.requireReviewOwner(adminA, studentA));
        assertThrows(AccessDeniedException.class, () -> access.requireReviewOwner(adminA, studentB));
        assertDoesNotThrow(() -> access.requireReviewOwner(studentB, studentB));
    }
    @Test void assignmentUsesExistingCourseAndReactivatesInsteadOfDuplicatingContent() {
        User admin = user(9, "SUPER_ADMIN", branchA); authenticate(admin);
        CourseAssignment existing = new CourseAssignment(); existing.setAssignmentId(4L); existing.setCourse(course); existing.setBranch(branchB); existing.setActiveRow(false);
        when(mappings.findByCourse_CourseIdAndBranch_BranchId(101L, 20L)).thenReturn(Optional.of(existing));
        when(mappings.save(any())).thenAnswer(c -> c.getArgument(0));
        var dto = assignments.assign(101L, 2L, 20L, true);
        assertEquals(4L, dto.assignmentId()); assertEquals(101L, dto.courseId()); assertTrue(dto.activeRow());
        assertSame(course, existing.getCourse()); verify(courses, never()).save(any());
    }
    @Test void branchAdminCannotAssignCourseToAnotherBranchOrSpoofCollege() {
        User admin = user(9, "BRANCH_ADMIN", branchA); authenticate(admin);
        assertThrows(AccessDeniedException.class, () -> assignments.assign(101L, 2L, 20L, true));
        authenticate(user(10, "SUPER_ADMIN", branchA));
        assertThrows(IllegalArgumentException.class, () -> assignments.assign(101L, 1L, 20L, true));
        verify(mappings, never()).save(any());
    }
    @Test void globalCourseCanBeCreatedBeforeChoosingAnyBranch() {
        CourseService service = new CourseService(courses, branches, mock(CollegeRepository.class));
        ReflectionTestUtils.setField(service, "assignmentService", assignments);
        CourseRequestDTO request = new CourseRequestDTO(); request.setName("Shared Commerce");
        assertDoesNotThrow(() -> service.create(request));
        var capture = org.mockito.ArgumentCaptor.forClass(Course.class); verify(courses).save(capture.capture());
        assertNull(capture.getValue().getCollege()); assertNull(capture.getValue().getBranch());
        verify(mappings, never()).save(any());
    }
    @Test void enrollmentRequiresAssignmentAndConsistentCollegeBranch() {
        assertDoesNotThrow(() -> assignments.requireAssignment(2L, 20L, 101L));
        assertThrows(IllegalArgumentException.class, () -> assignments.requireAssignment(2L, 30L, 101L));
        assertThrows(IllegalArgumentException.class, () -> assignments.requireAssignment(1L, 20L, 101L));
    }
    @Test void generatedMockTestsOnlySelectSubscribedQuestions() {
        var mcqs = mock(McqQuestionRepository.class); var mcqService = mock(McqQuestionService.class);
        var entitled = mock(SubscriptionEntitlementService.class);
        var service = new MockTestService(questions, mcqs, mcqService, entitled);
        when(questions.findByActiveRowTrue()).thenReturn(List.of(question(11L), question(12L)));
        McqQuestion mcq = new McqQuestion(); mcq.setActiveRow(true); when(mcqs.findById(anyLong())).thenReturn(Optional.of(mcq));
        when(entitled.accessibleQuestionIds(studentA.getEmail(), 101L)).thenReturn(Set.of(11L));
        when(entitled.requireCourseAccess(studentA.getEmail(), 101L)).thenReturn(personal.get(1L));
        McqQuestionResponseDTO dto = new McqQuestionResponseDTO(); dto.setQuestionId(11L); dto.setOptions(List.of());
        when(mcqService.getMcqQuestionById(11L)).thenReturn(dto);
        MockTestStartRequestDTO request = new MockTestStartRequestDTO(); request.setCourseId(101L); request.setQuestionCount(2);
        var result = service.start(request, studentA.getEmail());
        assertEquals(1, result.getQuestionCount()); assertEquals(11L, result.getQuestions().get(0).getQuestionId());
        verify(entitled).consumeMockTest(1L, 101L);
    }
    @Test void courseUploadReusesOneCourseForRowsFromDifferentBranches() {
        var mapper = mock(com.project.ProjectS.mapper.CourseExcelMapper.class);
        var processor = new com.project.ProjectS.processor.CourseExcelProcessor(mapper, courses);
        ReflectionTestUtils.setField(processor, "assignments", assignments);
        Map<String, String> first = Map.of("course_name", "Shared", "branch_id", "10");
        Map<String, String> second = Map.of("course_name", "Shared", "branch_id", "20");
        Course candidateA = course(); candidateA.setBranch(branchA);
        Course candidateB = course(); candidateB.setBranch(branchB);
        when(mapper.map(first)).thenReturn(candidateA); when(mapper.map(second)).thenReturn(candidateB);
        when(courses.findByNameIgnoreCase("Shared")).thenReturn(List.of(course));
        when(mappings.save(any())).thenAnswer(call -> call.getArgument(0));
        processor.process(List.of(first, second));
        var capture = org.mockito.ArgumentCaptor.forClass(CourseAssignment.class);
        verify(mappings, times(2)).save(capture.capture());
        assertTrue(capture.getAllValues().stream().allMatch(row -> row.getCourse() == course));
        assertEquals(Set.of(10L, 20L), new HashSet<>(capture.getAllValues().stream().map(row -> row.getBranch().getBranchId()).toList()));
        verify(courses, never()).save(any());
    }
    @Test void creatingAnExistingCourseRequestsAssignmentInsteadOfDuplicatingContent() {
        CourseService service = new CourseService(courses, branches, mock(CollegeRepository.class));
        when(courses.findByNameIgnoreCase("Shared")).thenReturn(List.of(course));
        CourseRequestDTO request = new CourseRequestDTO(); request.setName(" Shared ");
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(courses, never()).save(any());
    }
    @Test void sharedQuestionAnswersRemainPrivateToLearnerAndScopedAdministrators() {
        authenticate(studentA);
        assertTrue(access.canReadLearner(studentA)); assertFalse(access.canReadLearner(studentB));
        authenticate(user(8, "COLLEGE_ADMIN", branchA));
        assertTrue(access.canReadLearner(studentA)); assertFalse(access.canReadLearner(studentB));
    }
    private void authenticate(User user) {
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), "", List.of()));
    }
    private static Course course() { Course c = new Course(); c.setCourseId(101L); c.setName("Shared"); c.setActiveRow(true); return c; }
    private static Branch branch(long id, long collegeId) { College c = new College(); c.setCollegeId(collegeId); c.setInstituteName("College "+collegeId); Branch b = new Branch(); b.setBranchId(id); b.setCollege(c); b.setBranchName("Branch "+id); b.setActiveRow(true); return b; }
    private static User user(long id, String roleName, Branch branch) { Role r = new Role(); r.setRoleName(roleName); User u = new User(); u.setUserId(id); u.setEmail(id+"@test.example"); u.setRole(r); u.setBranch(branch); u.setCollege(branch.getCollege()); u.setActiveRow(true); return u; }
    private Question question(long id) { Question q = new Question(); q.setQuestionId(id); q.setCourse(course); q.setActiveRow(true); return q; }
    private UserSubscription subscription(User user) { SubscriptionPlan p = new SubscriptionPlan(); p.setPlanId(1L); p.setName("Basic"); p.setActive(true); p.setMockTestEnabled(true); p.setExamEnabled(true); p.setMockTestLimit(1); p.setExamAttemptLimit(1); p.setPracticeQuestionLimit(1); UserSubscription s = new UserSubscription(); s.setSubscriptionId(user.getUserId()*10); s.setUser(user); s.setCourse(course); s.setPlan(p); s.setActive(true); s.setStartsAt(LocalDateTime.now().minusDays(1)); s.setExpiresAt(LocalDateTime.now().plusDays(10)); return s; }
}
