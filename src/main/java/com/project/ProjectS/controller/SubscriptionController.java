package com.project.ProjectS.controller;

import com.project.ProjectS.model.*;
import com.project.ProjectS.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService service;
    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @GetMapping("/courses/{courseId}/plans")
    public List<SubscriptionPlanResponseDTO> activePlans(@PathVariable Long courseId) {
        return service.activePlans(courseId);
    }
    @GetMapping("/plans/course/{courseId}")
    public List<SubscriptionPlanResponseDTO> activePlansByCourse(@PathVariable Long courseId) {
        return service.activePlans(courseId);
    }
    @GetMapping("/plans")
    public List<SubscriptionPlanResponseDTO> plans(@RequestParam(required = false) Long courseId) {
        return service.plansForCourseOrAll(courseId);
    }
    @PostMapping("/plans")
    public ResponseEntity<SubscriptionPlanResponseDTO> create(@RequestBody SubscriptionPlanRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping("/plans/{id}")
    public SubscriptionPlanResponseDTO update(@PathVariable Long id, @RequestBody SubscriptionPlanRequestDTO request) {
        return service.update(id, request);
    }
    @PostMapping("/plans/{planId}/courses/{courseId}")
    public ResponseEntity<Void> assignCourse(@PathVariable Long planId, @PathVariable Long courseId) {
        service.assignCourse(planId, courseId); return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("/plans/{planId}/courses/{courseId}")
    public ResponseEntity<Void> removeCourse(@PathVariable Long planId, @PathVariable Long courseId) {
        service.removeCourse(planId, courseId); return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/activate")
    public UserSubscriptionResponseDTO activate(@RequestBody SubscriptionActivationRequestDTO request,
                                                Authentication authentication) {
        return request.getUserId() == null
                ? service.activateForEmail(request, authentication.getName())
                : service.activate(request);
    }
    @PostMapping("/bulk-assign")
    public List<UserSubscriptionResponseDTO> bulkAssign(@RequestBody BulkSubscriptionAssignmentDTO request) {
        return service.bulkAssign(request);
    }
    @PostMapping("/bulk")
    public List<UserSubscriptionResponseDTO> bulk(@RequestBody BulkSubscriptionAssignmentDTO request) {
        return service.bulkAssign(request);
    }
    @GetMapping("/users/{userId}")
    public List<UserSubscriptionResponseDTO> userSubscriptions(@PathVariable Long userId) {
        return service.forUser(userId);
    }

    @GetMapping("/mine")
    public List<UserSubscriptionResponseDTO> mySubscriptions(Authentication authentication) {
        return service.forEmail(authentication.getName());
    }

    @GetMapping("/history")
    public List<SubscriptionHistoryResponseDTO> history(Authentication authentication) {
        boolean superAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
        return service.history(authentication.getName(), superAdmin);
    }

    @PostMapping("/{subscriptionId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long subscriptionId, Authentication authentication) {
        boolean superAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
        service.deactivate(subscriptionId, authentication.getName(), superAdmin);
        return ResponseEntity.noContent().build();
    }
}
