package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentPerformanceDTO {

    private Long userId;
    private String studentName;

    private Long collegeId;
    private String collegeName;

    private Long branchId;
    private String branchName;

    private Long sectionId;
    private String sectionName;

    private Integer examsAttempted;

    private Double averagePercentage;
    private Double highestPercentage;
    private Double lowestPercentage;

    private Integer passedExams;
    private Integer failedExams;

    private Double passRate;
}