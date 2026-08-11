package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AnswerEventResponseDTO {

    private Long answerEventId;

    private Long questionId;

    private Long answerId;

    private Long tableNameId;
    private String tableName;

    private Long headerId;
    private String headerName;

    private Long attributeId;
    private String attributeName;

    private String arithmetic;

    private BigDecimal amount;

    private Boolean valid;

    private String description;

    private String action;

    private String userAnswer;

    private String answerBy;

    private String hint;

    private Boolean activeRow;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}