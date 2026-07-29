package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class QuestionAttributeResponseDTO {

    private Long questionAttributeId;

    private Long headerId;
    private String headerName;

    private Long attributeId;
    private String attributeName;

    private LocalDate transactionDate;

    private BigDecimal amount;

    private BigDecimal amount2;

    private String note;

    private Boolean activeRow;
}