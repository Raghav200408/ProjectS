package com.project.ProjectS.service;

import com.project.ProjectS.entity.UserSubscription;
import com.project.ProjectS.repository.UserSubscriptionRepository;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.repository.PlanCourseRepository;
import com.project.ProjectS.repository.PlanQuestionRepository;
import com.project.ProjectS.repository.QuestionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubscriptionEntitlementService {
    private static final Logger log = LogManager.getLogger(SubscriptionEntitlementService.class);
    private final UserSubscriptionRepository subscriptions;
    private final UserRepository users;
    private final PlanCourseRepository planCourses;
    private final PlanQuestionRepository planQuestions;
    private final QuestionRepository questions;

    public SubscriptionEntitlementService(UserSubscriptionRepository subscriptions, UserRepository users,
                                          PlanCourseRepository planCourses, PlanQuestionRepository planQuestions,
                                          QuestionRepository questions) {
        this.subscriptions = subscriptions;
        this.users = users;
        this.planCourses = planCourses;
        this.planQuestions = planQuestions;
        this.questions = questions;
    }

    @Transactional(readOnly = true)
    public UserSubscription requireCourseAccess(Long userId, Long courseId) {
        return subscriptions.findEntitledSubscription(userId, courseId, LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "An active subscription is required for this course"));
    }

    public UserSubscription requireCourseAccess(String email, Long courseId) {
        Long userId = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"))
                .getUserId();
        return requireCourseAccess(userId, courseId);
    }

    @Transactional(readOnly = true)
    public java.util.Set<Long> accessibleQuestionIds(String email, Long courseId) {
        UserSubscription subscription = requireCourseAccess(email, courseId);
        var mapping = planCourses.findByPlan_PlanIdAndCourse_CourseId(
                subscription.getPlan().getPlanId(), courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription plan is not configured for this course"));
        if (mapping.isAllCourseQuestions()) {
            return questions.findByCourse_CourseIdAndActiveRowTrue(courseId).stream()
                    .map(q -> q.getQuestionId()).collect(java.util.stream.Collectors.toSet());
        }
        java.util.Set<Long> selected = planQuestions.findByPlanCourse_PlanCourseId(mapping.getPlanCourseId()).stream()
                .map(pq -> pq.getQuestion().getQuestionId())
                .collect(java.util.stream.Collectors.toSet());
        if (selected.isEmpty()) return selected;
        return questions.findByCourse_CourseIdAndQuestionIdInAndActiveRowTrue(courseId, new java.util.ArrayList<>(selected))
                .stream().map(q -> q.getQuestionId()).collect(java.util.stream.Collectors.toSet());
    }

    public void requireQuestionAccess(String email, Long courseId, Long questionId) {
        if (!accessibleQuestionIds(email, courseId).contains(questionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This question is not included in your active subscription");
        }
    }

    @Transactional(readOnly = true)
    public void requireExamAttemptAvailable(String email, Long courseId) {
        UserSubscription subscription = requireCourseAccess(email, courseId);
        if (!subscription.getPlan().isExamEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exams are not included in this subscription");
        }
        Integer limit = subscription.getPlan().getExamAttemptLimit();
        if (isLimited(limit) && used(subscription.getExamAttemptsUsed()) >= limit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exam-attempt limit reached for this subscription");
        }
    }

    @Transactional(readOnly = true)
    public void requireMockTestAvailable(String email, Long courseId) {
        UserSubscription subscription = requireCourseAccess(email, courseId);
        if (!subscription.getPlan().isMockTestEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mock tests are not included in this subscription");
        }
        Integer limit = subscription.getPlan().getMockTestLimit();
        if (isLimited(limit) && used(subscription.getMockTestsUsed()) >= limit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mock-test limit reached for this subscription");
        }
    }

    @Transactional
    public void consumePracticeQuestions(Long userId, Long courseId, int count) {
        if (count <= 0) return;
        UserSubscription subscription = requireCourseAccess(userId, courseId);
        Integer limit = subscription.getPlan().getPracticeQuestionLimit();
        if (isLimited(limit) && used(subscription.getPracticeQuestionsUsed()) + count > limit) {
            throw new IllegalStateException("Practice-question limit reached for this subscription");
        }
        subscription.setPracticeQuestionsUsed(used(subscription.getPracticeQuestionsUsed()) + count);
        subscriptions.save(subscription);
    }

    @Transactional
    public void consumeMockTest(Long userId, Long courseId) {
        UserSubscription subscription = requireCourseAccess(userId, courseId);
        if (!subscription.getPlan().isMockTestEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mock tests are not included in this subscription");
        }
        Integer limit = subscription.getPlan().getMockTestLimit();
        if (isLimited(limit) && used(subscription.getMockTestsUsed()) + 1 > limit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mock-test limit reached for this subscription");
        }
        subscription.setMockTestsUsed(used(subscription.getMockTestsUsed()) + 1);
        subscriptions.save(subscription);
    }

    @Transactional
    public void consumeExamAttempt(Long userId, Long courseId) {
        UserSubscription subscription = requireCourseAccess(userId, courseId);
        if (!subscription.getPlan().isExamEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exams are not included in this subscription");
        }
        Integer limit = subscription.getPlan().getExamAttemptLimit();
        if (isLimited(limit) && used(subscription.getExamAttemptsUsed()) + 1 > limit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exam-attempt limit reached for this subscription");
        }
        subscription.setExamAttemptsUsed(used(subscription.getExamAttemptsUsed()) + 1);
        subscriptions.save(subscription);
    }

    private boolean isLimited(Integer limit) {
        return limit != null && limit >= 0;
    }

    private int used(Integer value) {
        return value == null ? 0 : value;
    }
}
