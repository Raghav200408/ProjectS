package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SubscriptionHistoryResponseDTO {
    private Long subscriptionId;
    private String studentName;
    private String studentEmail;
    private String branch;
    private String course;
    private String plan;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private String status;
}
