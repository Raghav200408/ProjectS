package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FillInTheBlankAnswerRequestDTO {

    private Long answerId;

    private String answerText;

    private Integer displayOrder;
}