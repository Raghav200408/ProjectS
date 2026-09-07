package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SubscriptionActivationRequestDTO {
    private Long userId;
    private Long courseId;
    private Long planId;
    /** Use FREE_TRIAL to select the course's free-trial plan. */
    private String activationType;
}
