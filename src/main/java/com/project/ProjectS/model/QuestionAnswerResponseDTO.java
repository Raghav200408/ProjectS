package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class QuestionAnswerResponseDTO {
    private Long answerId;
    private Long userId;
    private Long questionId;
    private Long attributeId;
    private String attributeName;
    private Long pairAttributeId;
    private String pairAttributeName;
    private Long totalAnswers;
    private Long conditionId;
    private Long tableNameId;
    private String tableName;
    private Long headerId;
    private String headerName;
    private String arithmetic;
    private BigDecimal amount;
    private Boolean activeRow;
    private LocalDateTime createdAt;
    private Integer rowStatus;
    private LocalDateTime updatedAt;


}