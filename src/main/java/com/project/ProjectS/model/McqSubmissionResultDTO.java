package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class McqSubmissionResultDTO {

    private Long questionId;

    private List<Long> selectedOptionIds;

    private List<Long> correctOptionIds;

    private String status;

    private BigDecimal marksAwarded;
}