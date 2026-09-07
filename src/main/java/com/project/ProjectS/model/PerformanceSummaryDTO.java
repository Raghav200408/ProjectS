package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceSummaryDTO {

    private Integer totalStudents;
    private Integer examsConducted;

    private Double averagePercentage;
    private Double passRate;

    private Integer passedResults;
    private Integer failedResults;
}
