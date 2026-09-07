package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class SubscriptionPlanRequestDTO {
    private String name;
    private String description;
    private boolean freeTrial;
    private boolean active = true;
    private Integer durationDays;
    private Integer practiceQuestionLimit;
    private boolean mockTestEnabled;
    private Integer mockTestLimit;
    private boolean examEnabled;
    private Integer examAttemptLimit;
    private List<Long> courseIds;
}
