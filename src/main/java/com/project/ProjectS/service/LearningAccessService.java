package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@Service
public class LearningAccessService {
    private final CourseAssignmentService assignments;
    private final SubscriptionEntitlementService entitlements;
    private final UserRepository users;
    private final QuestionRepository questions;
    private final SubscriptionQuestionUsageRepository usage;
    public LearningAccessService(CourseAssignmentService assignments, SubscriptionEntitlementService entitlements,
            UserRepository users, QuestionRepository questions, SubscriptionQuestionUsageRepository usage) {
        this.assignments = assignments; this.entitlements = entitlements; this.users = users; this.questions = questions; this.usage = usage;
    }
    // The subscription row is locked by requireCourseAccess. Repeated effects,
    // hints, retries and answer/event API pairs never charge the question twice.
    @Transactional
    public void practice(Long userId, Long questionId) {
        User user = users.findById(userId).orElseThrow(() -> new AccessDeniedException("User not found"));
        Question question = questions.findById(questionId).orElseThrow(() -> new IllegalArgumentException("Question not found"));
        assignments.requireEligible(user, question.getCourse());
        if (CourseAssignmentService.isManager(user)) return;
        UserSubscription subscription = entitlements.requireCourseAccess(userId, question.getCourse().getCourseId());
        entitlements.requireQuestionAccess(user.getEmail(), question.getCourse().getCourseId(), questionId);
        if (!usage.existsBySubscription_SubscriptionIdAndQuestion_QuestionId(subscription.getSubscriptionId(), questionId)) {
            entitlements.consumePracticeQuestions(userId, question.getCourse().getCourseId(), 1);
            SubscriptionQuestionUsage record = new SubscriptionQuestionUsage(); record.setSubscription(subscription); record.setQuestion(question);
            usage.save(record);
        }
    }
    public void requireExamAudience(User user, Exam exam) {
        assignments.requireEligible(user, exam.getCourse());
        if (CourseAssignmentService.isManager(user)) {
            assignments.requireOrganization(user, exam.getCollege().getCollegeId(), exam.getBranch().getBranchId());
        } else if (user.getSection() == null || !user.getSection().getSectionId().equals(exam.getSection().getSectionId()) ||
                user.getBranch() == null || !user.getBranch().getBranchId().equals(exam.getBranch().getBranchId()) ||
                user.getCollege() == null || !user.getCollege().getCollegeId().equals(exam.getCollege().getCollegeId())) {
            throw new AccessDeniedException("Exam is not assigned to your section");
        }
    }
    public boolean canReadLearner(User learner) {
        try {
            requireReviewOwner(assignments.currentUser(), learner);
            return true;
        } catch (AccessDeniedException denied) {
            return false;
        }
    }
    public void requireReadableLearner(User learner) {
        requireReviewOwner(assignments.currentUser(), learner);
    }
    public void requireReviewOwner(User caller, User learner) {
        if (caller == null) throw new AccessDeniedException("Sign in is required");
        if (caller.getUserId().equals(learner.getUserId())) return;
        assignments.requireOrganization(caller, learner.getCollege() == null ? null : learner.getCollege().getCollegeId(),
                learner.getBranch() == null ? null : learner.getBranch().getBranchId());
    }
}
