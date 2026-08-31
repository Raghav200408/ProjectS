package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class McqAnswerSubmissionDTO {

    private Long questionId;

    private List<Long> selectedOptionIds;
}