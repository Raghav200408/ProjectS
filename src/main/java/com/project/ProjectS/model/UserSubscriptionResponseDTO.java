package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class UserSubscriptionResponseDTO {
    private Long subscriptionId;
    private Long userId;
    private Long courseId;
    private Long planId;
    private String planName;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private Integer practiceQuestionLimit;
    private boolean mockTestEnabled;
    private Integer mockTestLimit;
    private boolean examEnabled;
    private Integer examAttemptLimit;
}
