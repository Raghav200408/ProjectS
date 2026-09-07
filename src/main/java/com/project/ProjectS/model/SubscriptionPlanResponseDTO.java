package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class SubscriptionPlanResponseDTO {
    private Long planId;
    private String name;
    private String description;
    private boolean freeTrial;
    private boolean active;
    private Integer durationDays;
    private Integer practiceQuestionLimit;
    private boolean mockTestEnabled;
    private Integer mockTestLimit;
    private boolean examEnabled;
    private Integer examAttemptLimit;
    private List<Long> courseIds;
}
