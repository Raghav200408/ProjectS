package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class QuestionAttributeRequestDTO {

    private Long headerId;

    private Long attributeId;

    private LocalDate transactionDate;

    private BigDecimal amount;

    private BigDecimal amount2;

    private String note;
}