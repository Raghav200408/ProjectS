package com.project.ProjectS.service;

import com.project.ProjectS.entity.UserSubscription;
import com.project.ProjectS.repository.UserSubscriptionRepository;
import com.project.ProjectS.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SubscriptionEntitlementService {
    private static final Logger log = LogManager.getLogger(SubscriptionEntitlementService.class);
    private final UserSubscriptionRepository subscriptions;
    private final UserRepository users;
    @org.springframework.beans.factory.annotation.Autowired private CourseAssignmentService assignments;

    public SubscriptionEntitlementService(UserSubscriptionRepository subscriptions, UserRepository users) {
        this.subscriptions = subscriptions;
        this.users = users;
    }

    @Transactional
    public UserSubscription requireCourseAccess(Long userId, Long courseId) {
        UserSubscription subscription = subscriptions.findEntitledSubscription(userId, courseId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalStateException("An active subscription is required for this course"));
        if (!subscription.getPlan().isActive()) throw new IllegalStateException("Subscription plan is inactive");
        LocalDateTime now = LocalDateTime.now();
        if (!subscription.isActive() || subscription.getStartsAt() == null || subscription.getStartsAt().isAfter(now) ||
                (subscription.getExpiresAt() != null && !subscription.getExpiresAt().isAfter(now)))
            throw new IllegalStateException("Subscription is inactive, expired or has not started");
        assignments.requireEligible(subscription.getUser(), subscription.getCourse());
        return subscription;
    }

    public UserSubscription requireCourseAccess(String email, Long courseId) {
        Long userId = users.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getUserId();
        return requireCourseAccess(userId, courseId);
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
            throw new IllegalStateException("Mock tests are not included in this subscription");
        }
        Integer limit = subscription.getPlan().getMockTestLimit();
        if (isLimited(limit) && used(subscription.getMockTestsUsed()) + 1 > limit) {
            throw new IllegalStateException("Mock-test limit reached for this subscription");
        }
        subscription.setMockTestsUsed(used(subscription.getMockTestsUsed()) + 1);
        subscriptions.save(subscription);
    }

    @Transactional
    public void consumeExamAttempt(Long userId, Long courseId) {
        UserSubscription subscription = requireCourseAccess(userId, courseId);
        if (!subscription.getPlan().isExamEnabled()) {
            throw new IllegalStateException("Exams are not included in this subscription");
        }
        Integer limit = subscription.getPlan().getExamAttemptLimit();
        if (isLimited(limit) && used(subscription.getExamAttemptsUsed()) + 1 > limit) {
            throw new IllegalStateException("Exam-attempt limit reached for this subscription");
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
