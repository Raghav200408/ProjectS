package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchPerformanceDTO {

    private Long branchId;
    private String branchName;

    private Integer totalStudents;
    private Integer examsConducted;

    private Double averagePercentage;
    private Double passRate;

    private Integer passedResults;
    private Integer failedResults;
}
