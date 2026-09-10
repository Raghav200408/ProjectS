package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class McqSubmissionRequestDTO {

    private Long userId;

    private Long questionId;

    private List<Long> selectedOptionIds;

    private boolean mockTest;
}
