package com.project.ProjectS.controller;

import com.project.ProjectS.model.NotificationResponseDTO;
import com.project.ProjectS.model.AnnouncementRequestDTO;
import jakarta.validation.Valid;
import com.project.ProjectS.security.service.CustomUserDetails;
import com.project.ProjectS.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/announcements")
    public Map<String, Integer> announce(@AuthenticationPrincipal CustomUserDetails principal,
                                        @Valid @RequestBody AnnouncementRequestDTO request) {
        return Map.of("recipientCount", service.announce(principal == null ? null : principal.getUser(), request));
    }

    @GetMapping("/mine")
    public List<NotificationResponseDTO> mine(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "20") int limit) {
        return service.mine(principal.getUser(), limit);
    }

    @GetMapping("/mine/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        return Map.of("unreadCount", service.unreadCount(principal.getUser()));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        service.markRead(principal.getUser(), notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/mine/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal CustomUserDetails principal) {
        service.markAllRead(principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
