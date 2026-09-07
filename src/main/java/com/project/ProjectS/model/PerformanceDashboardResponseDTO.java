package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PerformanceDashboardResponseDTO {

    private Integer totalStudents;
    private Integer examsConducted;

    private Double averagePercentage;
    private Double passRate;

    private Integer passedResults;
    private Integer failedResults;

    private List<BranchPerformanceDTO> branchPerformance;
    private List<CoursePerformanceDTO> coursePerformance;
    private List<PerformanceTrendDTO> performanceTrend;

    private List<StudentPerformanceDTO> topPerformers;
    private List<StudentPerformanceDTO> studentsNeedingAttention;
}