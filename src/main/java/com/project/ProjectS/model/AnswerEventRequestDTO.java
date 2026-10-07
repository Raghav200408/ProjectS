package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class AnswerEventRequestDTO {

    private Long userId;

    private Long questionId;

    private Long attributeId;

    private Long questionAttributeId;
    private Long tableNameId;
    private Long headerId;
    private String tableName;
    private String headerName;
    private BigDecimal amount;
    private Long conditionId;

    private Integer answerPosition;

    private String arithmetic;

    private String eventType;

    private Boolean isCorrect;

    private String hint;

    private String description;

    private String userAnswer;
}
