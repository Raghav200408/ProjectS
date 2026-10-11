package com.project.ProjectS.controller;

import com.project.ProjectS.model.StudentLoginInactivityDTO;
import com.project.ProjectS.security.service.CustomUserDetails;
import com.project.ProjectS.service.StudentLoginAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/student-logins")
public class StudentLoginAnalyticsController {
    private final StudentLoginAnalyticsService service;

    public StudentLoginAnalyticsController(StudentLoginAnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/inactive")
    public ResponseEntity<StudentLoginInactivityDTO> inactiveStudents(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long sectionId) {
        return ResponseEntity.ok(service.getInactiveStudents(
                principal.getUser(), days, collegeId, branchId, sectionId));
    }
}
