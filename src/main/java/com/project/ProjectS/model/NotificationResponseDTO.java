package com.project.ProjectS.model;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long notificationId,
        String eventType,
        String title,
        String message,
        String severity,
        String actionUrl,
        boolean read,
        LocalDateTime createdAt
) {
}
