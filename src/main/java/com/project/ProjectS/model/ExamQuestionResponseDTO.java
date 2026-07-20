package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExamQuestionResponseDTO {

    private Long questionId;

    private Long examId;

    private Long headerId;

    private Long attributeId;

    private LocalDate transactionDate;

    private BigDecimal amount;

    private BigDecimal amount2;

    private String externalFile;

    private String note;

    private Boolean activeRow;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}