package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoursePerformanceDTO {

    private Long courseId;
    private String courseName;

    private Integer totalStudents;
    private Integer examsConducted;

    private Double averagePercentage;
    private Double passRate;

    private Integer passedResults;
    private Integer failedResults;
}