package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PracticeStudentSupportDTO {
    private Long userId;
    private String studentName;
    private Long studentCode;
    private String sectionName;
    private long totalUnits;
    private long attemptedUnits;
    private long correctUnits;
    private BigDecimal accuracyPercentage;
}
