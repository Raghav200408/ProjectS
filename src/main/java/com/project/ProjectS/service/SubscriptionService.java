package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {
    private final SubscriptionPlanRepository plans;
    private final PlanCourseRepository planCourses;
    private final UserSubscriptionRepository subscriptions;
    private final CourseRepository courses;
    private final UserRepository users;

    public SubscriptionService(SubscriptionPlanRepository plans, PlanCourseRepository planCourses,
                                UserSubscriptionRepository subscriptions, CourseRepository courses,
                                UserRepository users) {
        this.plans = plans; this.planCourses = planCourses; this.subscriptions = subscriptions;
        this.courses = courses; this.users = users;
    }

    public List<SubscriptionPlanResponseDTO> activePlans(Long courseId) {
        return planCourses.findByCourse_CourseIdAndPlan_ActiveTrue(courseId).stream()
                .map(pc -> toPlan(pc.getPlan())).distinct().collect(Collectors.toList());
    }

    public List<SubscriptionPlanResponseDTO> plansForCourseOrAll(Long courseId) {
        return courseId == null ? allPlans() : activePlans(courseId);
    }

    @Transactional
    public SubscriptionPlanResponseDTO create(SubscriptionPlanRequestDTO request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        copy(plan, request);
        plan = plans.save(plan);
        replaceCourses(plan, request.getCourseIds());
        return toPlan(plan);
    }

    @Transactional
    public SubscriptionPlanResponseDTO update(Long id, SubscriptionPlanRequestDTO request) {
        SubscriptionPlan plan = plans.findById(id).orElseThrow(() -> new NoSuchElementException("Subscription plan not found"));
        copy(plan, request);
        plans.save(plan);
        planCourses.deleteByPlan_PlanId(id);
        replaceCourses(plan, request.getCourseIds());
        return toPlan(plan);
    }

    public List<SubscriptionPlanResponseDTO> allPlans() {
        return plans.findAll().stream().map(this::toPlan).collect(Collectors.toList());
    }
    @Transactional
    public void delete(Long id) {
        SubscriptionPlan plan = plans.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subscription plan not found"));
        plan.setActive(false);
        plans.save(plan);
        subscriptions.findByPlan_PlanIdAndActiveTrue(id).forEach(subscription -> {
            subscription.setActive(false);
            subscriptions.save(subscription);
        });
    }

    @Transactional
    public void assignCourse(Long planId, Long courseId) {
        SubscriptionPlan plan = plans.findById(planId).orElseThrow(() -> new NoSuchElementException("Subscription plan not found"));
        Course course = courses.findById(courseId).orElseThrow(() -> new NoSuchElementException("Course not found"));
        if (planCourses.findByPlan_PlanId(planId).stream().noneMatch(pc -> pc.getCourse().getCourseId().equals(courseId))) {
            PlanCourse mapping = new PlanCourse(); mapping.setPlan(plan); mapping.setCourse(course); planCourses.save(mapping);
        }
    }

    @Transactional
    public void removeCourse(Long planId, Long courseId) {
        planCourses.findByPlan_PlanId(planId).stream()
                .filter(pc -> pc.getCourse().getCourseId().equals(courseId))
                .forEach(planCourses::delete);
    }

    @Transactional
    public UserSubscriptionResponseDTO activate(SubscriptionActivationRequestDTO request) {
        User user = users.findById(request.getUserId()).orElseThrow(() -> new NoSuchElementException("User not found"));
        Course course = courses.findById(request.getCourseId()).orElseThrow(() -> new NoSuchElementException("Course not found"));
        SubscriptionPlan plan = resolvePlan(request);
        if (!planCourses.findByCourse_CourseIdAndPlan_ActiveTrue(course.getCourseId()).stream()
                .anyMatch(pc -> pc.getPlan().getPlanId().equals(plan.getPlanId())))
            throw new IllegalArgumentException("Plan is not available for this course");
        subscriptions.findFirstByUser_UserIdAndCourse_CourseIdAndActiveTrue(user.getUserId(), course.getCourseId())
                .ifPresent(existing -> { existing.setActive(false); subscriptions.save(existing); });
        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user); subscription.setCourse(course); subscription.setPlan(plan);
        subscription.setExpiresAt(plan.getDurationDays() == null ? null :
                LocalDateTime.now().plusDays(plan.getDurationDays()));
        return toSubscription(subscriptions.save(subscription));
    }

    @Transactional
    public UserSubscriptionResponseDTO activateForEmail(SubscriptionActivationRequestDTO request, String email) {
        if (request.getUserId() == null) {
            request.setUserId(users.findByEmail(email)
                    .orElseThrow(() -> new NoSuchElementException("Authenticated user not found")).getUserId());
        }
        return activate(request);
    }

    @Transactional
    public List<UserSubscriptionResponseDTO> bulkAssign(BulkSubscriptionAssignmentDTO request) {
        if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) return List.of();
        List<UserSubscriptionResponseDTO> result = new ArrayList<>();
        for (Long studentId : request.getStudentIds()) {
            users.findByUserIdAndRole_RoleName(studentId, "STUDENT")
                    .orElseThrow(() -> new IllegalArgumentException("User is not an active student: " + studentId));
            SubscriptionActivationRequestDTO activation = new SubscriptionActivationRequestDTO();
            activation.setUserId(studentId); activation.setCourseId(request.getCourseId());
            activation.setPlanId(request.getPlanId());
            result.add(activate(activation));
        }
        return result;
    }

    public List<UserSubscriptionResponseDTO> forUser(Long userId) {
        return subscriptions.findByUser_UserIdAndActiveTrue(userId).stream().map(this::toSubscription).collect(Collectors.toList());
    }

    public List<UserSubscriptionResponseDTO> forEmail(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
        return forUser(user.getUserId());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionHistoryResponseDTO> history(String email, boolean allBranches) {
        List<UserSubscription> history;
        if (allBranches) {
            history = subscriptions.findAllByOrderByStartsAtDesc();
        } else {
            User user = users.findByEmail(email)
                    .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
            if (user.getBranch() == null) {
                return List.of();
            }
            history = subscriptions.findByUser_Branch_BranchIdOrderByStartsAtDesc(user.getBranch().getBranchId());
        }
        return history.stream().map(this::toHistory).collect(Collectors.toList());
    }

    @Transactional
    public void deactivate(Long subscriptionId, String adminEmail, boolean superAdmin) {
        UserSubscription subscription = subscriptions.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("Subscription not found"));
        if (!superAdmin) {
            User admin = users.findByEmail(adminEmail)
                    .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
            Long adminBranchId = admin.getBranch() == null ? null : admin.getBranch().getBranchId();
            Long studentBranchId = subscription.getUser().getBranch() == null
                    ? null : subscription.getUser().getBranch().getBranchId();
            if (adminBranchId == null || !adminBranchId.equals(studentBranchId)) {
                throw new IllegalArgumentException("You can only deactivate subscriptions for users in your branch");
            }
        }
        subscription.setActive(false);
        subscriptions.save(subscription);
    }

    private SubscriptionPlan resolvePlan(SubscriptionActivationRequestDTO request) {
        if ("FREE_TRIAL".equalsIgnoreCase(request.getActivationType()))
            return plans.findFirstByFreeTrialTrueAndActiveTrue().orElseThrow(() -> new NoSuchElementException("Free trial plan not found"));
        return plans.findById(request.getPlanId()).orElseThrow(() -> new NoSuchElementException("Subscription plan not found"));
    }
    private void copy(SubscriptionPlan p, SubscriptionPlanRequestDTO r) {
        p.setName(r.getName()); p.setDescription(r.getDescription()); p.setFreeTrial(r.isFreeTrial());
        p.setActive(r.isActive()); p.setDurationDays(r.getDurationDays());
        p.setPracticeQuestionLimit(r.getPracticeQuestionLimit()); p.setMockTestEnabled(r.isMockTestEnabled());
        p.setMockTestLimit(r.getMockTestLimit()); p.setExamEnabled(r.isExamEnabled());
        p.setExamAttemptLimit(r.getExamAttemptLimit());
    }
    private void replaceCourses(SubscriptionPlan plan, List<Long> ids) {
        if (ids != null) ids.forEach(id -> {
            PlanCourse pc = new PlanCourse(); pc.setPlan(plan);
            pc.setCourse(courses.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found: " + id)));
            planCourses.save(pc);
        });
    }
    private SubscriptionPlanResponseDTO toPlan(SubscriptionPlan p) {
        SubscriptionPlanResponseDTO d = new SubscriptionPlanResponseDTO();
        d.setPlanId(p.getPlanId()); d.setName(p.getName()); d.setDescription(p.getDescription());
        d.setFreeTrial(p.isFreeTrial()); d.setActive(p.isActive()); d.setDurationDays(p.getDurationDays());
        d.setPracticeQuestionLimit(p.getPracticeQuestionLimit()); d.setMockTestEnabled(p.isMockTestEnabled());
        d.setMockTestLimit(p.getMockTestLimit()); d.setExamEnabled(p.isExamEnabled()); d.setExamAttemptLimit(p.getExamAttemptLimit());
        d.setCourseIds(planCourses.findByPlan_PlanId(p.getPlanId()).stream().map(pc -> pc.getCourse().getCourseId()).toList());
        return d;
    }
    private UserSubscriptionResponseDTO toSubscription(UserSubscription s) {
        SubscriptionPlan p = s.getPlan(); UserSubscriptionResponseDTO d = new UserSubscriptionResponseDTO();
        d.setSubscriptionId(s.getSubscriptionId()); d.setUserId(s.getUser().getUserId()); d.setCourseId(s.getCourse().getCourseId());
        d.setPlanId(p.getPlanId()); d.setPlanName(p.getName()); d.setStartsAt(s.getStartsAt()); d.setExpiresAt(s.getExpiresAt()); d.setActive(s.isActive());
        d.setPracticeQuestionLimit(p.getPracticeQuestionLimit()); d.setMockTestEnabled(p.isMockTestEnabled()); d.setMockTestLimit(p.getMockTestLimit());
        d.setExamEnabled(p.isExamEnabled()); d.setExamAttemptLimit(p.getExamAttemptLimit()); return d;
    }

    private SubscriptionHistoryResponseDTO toHistory(UserSubscription s) {
        SubscriptionHistoryResponseDTO d = new SubscriptionHistoryResponseDTO();
        d.setSubscriptionId(s.getSubscriptionId());
        d.setStudentName(s.getUser().getName());
        d.setStudentEmail(s.getUser().getEmail());
        d.setBranch(s.getUser().getBranch() == null ? null : s.getUser().getBranch().getBranchName());
        d.setCourse(s.getCourse().getName());
        d.setPlan(s.getPlan().getName());
        d.setStartsAt(s.getStartsAt());
        d.setExpiresAt(s.getExpiresAt());
        d.setActive(s.isActive());
        d.setStatus(subscriptionStatus(s));
        return d;
    }

    private String subscriptionStatus(UserSubscription subscription) {
        if (!subscription.isActive()) {
            return "INACTIVE";
        }
        if (subscription.getExpiresAt() != null && subscription.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}
