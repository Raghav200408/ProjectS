package com.project.ProjectS.service;

import com.project.ProjectS.config.AuditLogger;
import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {
    private static final Logger log = LogManager.getLogger(SubscriptionService.class);
    private final SubscriptionPlanRepository plans;
    private final PlanCourseRepository planCourses;
    private final PlanQuestionRepository planQuestions;
    private final QuestionRepository questions;
    private final UserSubscriptionRepository subscriptions;
    private final CourseRepository courses;
    private final UserRepository users;
    private final AuditLogger auditLogger;

    public SubscriptionService(SubscriptionPlanRepository plans, PlanCourseRepository planCourses,
                                PlanQuestionRepository planQuestions, QuestionRepository questions,
                                UserSubscriptionRepository subscriptions, CourseRepository courses,
                                UserRepository users, AuditLogger auditLogger) {
        this.plans = plans; this.planCourses = planCourses; this.planQuestions = planQuestions;
        this.questions = questions; this.subscriptions = subscriptions;
        this.courses = courses; this.users = users;
        this.auditLogger = auditLogger;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDTO> activePlans(Long courseId) {
        log.info("Fetching active plans for courseId={}", courseId);
        return planCourses.findByCourse_CourseIdAndPlan_ActiveTrue(courseId).stream()
                .map(pc -> {
                    SubscriptionPlanResponseDTO dto = toPlan(pc.getPlan());
                    dto.setPracticeQuestionLimit(pc.isAllCourseQuestions() ? -1
                            : pc.getQuestionLimit() == null ? pc.getPlan().getPracticeQuestionLimit() : pc.getQuestionLimit());
                    dto.setAllCourseQuestions(pc.isAllCourseQuestions());
                    dto.setAvailableQuestionCount(pc.isAllCourseQuestions()
                            ? questions.findByCourse_CourseIdAndActiveRowTrue(courseId).size()
                            : (int) planQuestions.findByPlanCourse_PlanCourseId(pc.getPlanCourseId()).stream()
                                    .filter(pq -> Boolean.TRUE.equals(pq.getQuestion().getActiveRow())).count());
                    return dto;
                }).distinct().collect(Collectors.toList());
    }

    public List<SubscriptionPlanResponseDTO> plansForCourseOrAll(Long courseId) {
        return courseId == null ? allPlans() : activePlans(courseId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getQuestionAssignments(Long planId, Long courseId) {
        PlanCourse mapping = requirePlanCourse(planId, courseId);
        Integer configuredLimit = mapping.getQuestionLimit() == null
                ? mapping.getPlan().getPracticeQuestionLimit() : mapping.getQuestionLimit();
        return Map.of("questionLimit", configuredLimit == null ? -1 : configuredLimit,
                "allCourseQuestions", mapping.isAllCourseQuestions(),
                "questionIds", mapping.isAllCourseQuestions() ? List.of() : planQuestions
                        .findByPlanCourse_PlanCourseId(mapping.getPlanCourseId()).stream()
                        .map(pq -> pq.getQuestion().getQuestionId()).toList());
    }

    @Transactional
    public Map<String, Object> setQuestionAssignments(Long planId, Long courseId,
                                                        PlanQuestionAssignmentRequestDTO request) {
        PlanCourse mapping = requirePlanCourse(planId, courseId);
        int limit = request.getQuestionLimit() == null
                ? (mapping.getPlan().getPracticeQuestionLimit() == null ? -1 : mapping.getPlan().getPracticeQuestionLimit())
                : request.getQuestionLimit();
        List<Long> ids = request.getQuestionIds() == null ? List.of() : request.getQuestionIds().stream()
                .filter(Objects::nonNull).distinct().toList();
        if (limit < -1) throw new IllegalArgumentException("Question limit must be -1 (unlimited) or non-negative");
        if (request.isAllCourseQuestions() && limit != -1) {
            throw new IllegalArgumentException("All course questions requires an unlimited question limit");
        }
        if (!request.isAllCourseQuestions() && limit >= 0 && ids.size() > limit) {
            throw new IllegalArgumentException("Selected question count exceeds the plan limit");
        }
        if (!ids.isEmpty()) {
            Set<Long> found = questions.findByCourse_CourseIdAndQuestionIdInAndActiveRowTrue(courseId, ids)
                    .stream().map(Question::getQuestionId).collect(Collectors.toSet());
            if (found.size() != ids.size()) throw new IllegalArgumentException("Every selected question must be active and belong to this course");
        }
        validateCumulativeSet(mapping, request.isAllCourseQuestions(), new HashSet<>(ids));
        mapping.setQuestionLimit(limit);
        mapping.setAllCourseQuestions(request.isAllCourseQuestions());
        planCourses.save(mapping);
        planQuestions.deleteByPlanCourse_PlanCourseId(mapping.getPlanCourseId());
        if (!request.isAllCourseQuestions()) {
            for (Long id : ids) {
                PlanQuestion assignment = new PlanQuestion();
                assignment.setPlanCourse(mapping);
                assignment.setQuestion(questions.getReferenceById(id));
                planQuestions.save(assignment);
            }
        }
        return getQuestionAssignments(planId, courseId);
    }

    private PlanCourse requirePlanCourse(Long planId, Long courseId) {
        return planCourses.findByPlan_PlanIdAndCourse_CourseId(planId, courseId)
                .orElseThrow(() -> new NoSuchElementException("Plan is not assigned to this course"));
    }

    private void validateCumulativeSet(PlanCourse current, boolean allQuestions, Set<Long> selectedIds) {
        int currentTier = planTier(current.getPlan().getName());
        if (currentTier < 0) return;
        for (PlanCourse other : planCourses.findByCourse_CourseIdAndPlan_ActiveTrue(
                current.getCourse().getCourseId())) {
            int otherTier = planTier(other.getPlan().getName());
            if (other.getPlanCourseId().equals(current.getPlanCourseId()) || otherTier < 0 || otherTier == currentTier) continue;
            Set<Long> otherIds = planQuestions.findByPlanCourse_PlanCourseId(other.getPlanCourseId()).stream()
                    .map(row -> row.getQuestion().getQuestionId()).collect(Collectors.toSet());
            if (otherTier < currentTier) {
                if (other.isAllCourseQuestions() && !allQuestions) {
                    throw new IllegalArgumentException("This plan must include all questions included in the lower tier");
                }
                if (!allQuestions && !selectedIds.containsAll(otherIds)) {
                    throw new IllegalArgumentException("This plan must include all questions assigned to lower tiers");
                }
            }
            if (otherTier > currentTier) {
                if (allQuestions && !other.isAllCourseQuestions()) {
                    throw new IllegalArgumentException("Update higher tiers to include all questions before changing this plan to all-access");
                }
                if (!other.isAllCourseQuestions() && !otherIds.containsAll(selectedIds)) {
                    throw new IllegalArgumentException("Update higher-tier question sets before adding questions to this tier");
                }
            }
        }
    }

    private int planTier(String name) {
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (normalized.contains("free")) return 0;
        if (normalized.contains("basic")) return 1;
        if (normalized.contains("standard")) return 2;
        if (normalized.contains("premium")) return 3;
        return -1;
    }

    @Transactional
    public SubscriptionPlanResponseDTO create(SubscriptionPlanRequestDTO request) {
        log.info("Creating subscription plan: name={} active={} freeTrial={}", request.getName(), request.isActive(), request.isFreeTrial());
        SubscriptionPlan plan = new SubscriptionPlan();
        copy(plan, request);
        plan = plans.save(plan);
        replaceCourses(plan, request.getCourseIds());
        SubscriptionPlanResponseDTO response = toPlan(plan);
        log.info("Subscription plan created: planId={}", response.getPlanId());
        auditLogger.log("SUBSCRIPTION_PLAN_CHANGE", "admin", "PLAN", response.getPlanId(), "CREATED", "name=" + response.getName());
        return response;
    }

    @Transactional
    public SubscriptionPlanResponseDTO update(Long id, SubscriptionPlanRequestDTO request) {
        SubscriptionPlan plan = plans.findById(id).orElseThrow(() -> new NoSuchElementException("Subscription plan not found"));
        boolean reactivatingPlan = !plan.isActive() && request.isActive();
        copy(plan, request);
        plans.save(plan);
        syncCourses(plan, request.getCourseIds());
        if (reactivatingPlan) {
            subscriptions.findInactiveValidByPlan(id, LocalDateTime.now()).forEach(subscription -> {
                subscription.setActive(true);
                subscriptions.save(subscription);
            });
        }
        SubscriptionPlanResponseDTO response = toPlan(plan);
        log.info("Subscription plan updated: planId={}", response.getPlanId());
        auditLogger.log("SUBSCRIPTION_PLAN_CHANGE", "admin", "PLAN", response.getPlanId(), "UPDATED", "name=" + response.getName());
        return response;
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
            PlanCourse mapping = new PlanCourse();
            mapping.setPlan(plan);
            mapping.setCourse(course);
            mapping.setQuestionLimit(plan.getPracticeQuestionLimit());
            planCourses.save(mapping);
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
        log.info("Activating subscription: userId={} courseId={} planId={} activationType={}", request.getUserId(), request.getCourseId(), request.getPlanId(), request.getActivationType());
        User user = users.findById(request.getUserId()).orElseThrow(() -> new NoSuchElementException("User not found"));
        Course course = courses.findById(request.getCourseId()).orElseThrow(() -> new NoSuchElementException("Course not found"));
        SubscriptionPlan plan = resolvePlan(request, course.getCourseId());
        if (!planCourses.findByCourse_CourseIdAndPlan_ActiveTrue(course.getCourseId()).stream()
                .anyMatch(pc -> pc.getPlan().getPlanId().equals(plan.getPlanId())))
            throw new IllegalArgumentException("Plan is not available for this course");
        subscriptions.findFirstByUser_UserIdAndCourse_CourseIdAndActiveTrue(user.getUserId(), course.getCourseId())
                .ifPresent(existing -> { existing.setActive(false); subscriptions.save(existing); });
        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user); subscription.setCourse(course); subscription.setPlan(plan);
        subscription.setExpiresAt(plan.getDurationDays() == null ? null :
                LocalDateTime.now().plusDays(plan.getDurationDays()));
        UserSubscriptionResponseDTO response = toSubscription(subscriptions.save(subscription));
        log.info("Subscription activated successfully: subscriptionId={} userId={} courseId={}", response.getSubscriptionId(), response.getUserId(), response.getCourseId());
        auditLogger.log("SUBSCRIPTION_CHANGE", user.getEmail(), "SUBSCRIPTION", response.getSubscriptionId(), "ACTIVATED",
                "planId=" + response.getPlanId() + ", courseId=" + response.getCourseId());
        return response;
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
        auditLogger.log("SUBSCRIPTION_CHANGE", subscription.getUser().getEmail(), "SUBSCRIPTION", subscriptionId, "DEACTIVATED",
                "adminEmail=" + adminEmail + ", superAdmin=" + superAdmin);
    }

    private SubscriptionPlan resolvePlan(SubscriptionActivationRequestDTO request, Long courseId) {
        if (request == null) {
            throw new IllegalArgumentException("Subscription activation request is required");
        }
        if ("FREE_TRIAL".equalsIgnoreCase(request.getActivationType())) {
            return planCourses.findByCourse_CourseIdAndPlan_ActiveTrue(courseId).stream()
                    .map(PlanCourse::getPlan)
                    .filter(SubscriptionPlan::isFreeTrial)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Free trial plan not found for this course"));
        }
        if (request.getPlanId() == null) {
            throw new IllegalArgumentException("Subscription plan ID is required");
        }
        return plans.findById(request.getPlanId()).orElseThrow(() -> new NoSuchElementException("Subscription plan not found"));
    }
    private void copy(SubscriptionPlan p, SubscriptionPlanRequestDTO r) {
        if (r.getPrice() != null) {
            var price = r.getPrice().stripTrailingZeros();
            if (price.signum() < 0 || price.scale() > 2 || price.precision() - price.scale() > 10) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Price must be non-negative, below 10,000,000,000 and have at most two decimal places");
            }
            p.setPrice(r.getPrice());
        }
        p.setName(r.getName()); p.setDescription(r.getDescription()); p.setFreeTrial(r.isFreeTrial());
        p.setActive(r.isActive()); p.setDurationDays(r.getDurationDays());
        p.setPracticeQuestionLimit(r.getPracticeQuestionLimit()); p.setMockTestEnabled(r.isMockTestEnabled());
        p.setMockTestLimit(r.getMockTestLimit()); p.setExamEnabled(r.isExamEnabled());
        p.setExamAttemptLimit(r.getExamAttemptLimit());
    }
    private void replaceCourses(SubscriptionPlan plan, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Set<Long> seenCourseIds = new HashSet<>();
        for (Long id : ids) {
            if (id == null || !seenCourseIds.add(id)) {
                continue;
            }
            if (planCourses.findByPlan_PlanId(plan.getPlanId()).stream()
                    .anyMatch(pc -> pc.getCourse().getCourseId().equals(id))) {
                continue;
            }
            PlanCourse pc = new PlanCourse();
            pc.setPlan(plan);
            pc.setCourse(courses.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found: " + id)));
            planCourses.save(pc);
        }
    }

    private void syncCourses(SubscriptionPlan plan, List<Long> ids) {
        Set<Long> desired = ids == null ? Set.of() : ids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        for (PlanCourse mapping : planCourses.findByPlan_PlanId(plan.getPlanId())) {
            Long courseId = mapping.getCourse().getCourseId();
            if (!desired.contains(courseId)) {
                planQuestions.deleteByPlanCourse_PlanCourseId(mapping.getPlanCourseId());
                planCourses.delete(mapping);
            }
        }
        replaceCourses(plan, new ArrayList<>(desired));
    }
    private SubscriptionPlanResponseDTO toPlan(SubscriptionPlan p) {
        SubscriptionPlanResponseDTO d = new SubscriptionPlanResponseDTO();
        d.setPlanId(p.getPlanId()); d.setName(p.getName()); d.setDescription(p.getDescription());
        d.setPrice(p.getPrice());
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
