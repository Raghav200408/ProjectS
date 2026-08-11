package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerEventRequestDTO {

    private Long userId;

    private Long questionId;

    private Long attributeId;

    private Long tableNameId;

    private Long headerId;

    private String arithmetic;

    private String eventType;

    private Boolean isCorrect;

    private String hint;

    private String description;

    private String userAnswer;
}