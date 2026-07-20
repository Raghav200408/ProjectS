package com.project.ProjectS.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExamQuestionRequestDTO {

    @NotNull(message = "Exam Id is required")
    private Long examId;

    @NotNull(message = "Header Id is required")
    private Long headerId;

    @NotNull(message = "Attribute Id is required")
    private Long attributeId;

    private LocalDate transactionDate;

    private BigDecimal amount;

    private BigDecimal amount2;

    @Size(max = 500, message = "External file path must not exceed 500 characters")
    private String externalFile;

    private String note;

}