package com.project.ProjectS.controller;

import com.project.ProjectS.model.ActivitySessionRequestDTO;
import com.project.ProjectS.model.ActivitySessionResponseDTO;
import com.project.ProjectS.security.service.CustomUserDetails;
import com.project.ProjectS.service.UserActivitySessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activity-sessions")
public class UserActivitySessionController {
    private final UserActivitySessionService service;

    public UserActivitySessionController(UserActivitySessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ActivitySessionResponseDTO> start(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(service.start(principal.getUser()));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<ActivitySessionResponseDTO> heartbeat(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ActivitySessionRequestDTO request) {
        return ResponseEntity.ok(service.heartbeat(principal.getUser(), request.sessionKey()));
    }

    @PostMapping("/idle")
    public ResponseEntity<ActivitySessionResponseDTO> idle(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ActivitySessionRequestDTO request) {
        return ResponseEntity.ok(service.idle(principal.getUser(), request.sessionKey()));
    }

    @PostMapping("/resume")
    public ResponseEntity<ActivitySessionResponseDTO> resume(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ActivitySessionRequestDTO request) {
        return ResponseEntity.ok(service.resume(principal.getUser(), request.sessionKey()));
    }

    @PostMapping("/close")
    public ResponseEntity<ActivitySessionResponseDTO> close(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ActivitySessionRequestDTO request) {
        return ResponseEntity.ok(service.close(principal.getUser(), request.sessionKey()));
    }

    @GetMapping("/total-time")
    public ResponseEntity<Map<String, Long>> totalTime(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(Map.of("totalActiveSeconds", service.totalTime(principal.getUser())));
    }
}