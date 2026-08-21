package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QuestionAnswerRequestDTO {

    private Long userId;

    private Long questionId;

    private Long tableNameId;

    private Long headerId;

    private Long attributeId;

    private String arithmetic;

    private BigDecimal amount;

    private Long conditionId;

    private Long pairAttributeId;

    private Long totalAnswers;
}