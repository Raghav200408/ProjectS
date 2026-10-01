package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeEventResultRequestDTO {
    private Long answerEventId;
    // Matching source pair, or rule position for attribute events without a position.
    private Integer unitPosition;
}
