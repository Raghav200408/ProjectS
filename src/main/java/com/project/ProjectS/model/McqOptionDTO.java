package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McqOptionDTO {

    private Long optionId;

    private Integer optionOrder;

    private String optionText;

    private Boolean isCorrect;
}