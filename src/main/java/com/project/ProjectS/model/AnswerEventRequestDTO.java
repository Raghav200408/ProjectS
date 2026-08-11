package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AnswerEventRequestDTO {

    private Long userId;

    private Long questionId;

    private Long answerId;

    private Long tableNameId;

    private Long headerId;

    private Long attributeId;

    private String arithmetic;

    private BigDecimal amount;

    private String description;

    private String action;

    private String userAnswer;

    private String answerBy;

    private String hint;
}