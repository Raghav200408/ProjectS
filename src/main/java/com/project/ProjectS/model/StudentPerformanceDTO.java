package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class StudentPerformanceDTO {

    private Long userId;
    private String studentName;

    private Long collegeId;
    private String collegeName;

    private Long branchId;
    private String branchName;

    private Long courseId;
    private String courseName;

    private Long sectionId;
    private String sectionName;

    private Integer examsAttempted;

    private Double averagePercentage;
    private Double highestPercentage;
    private Double lowestPercentage;

    private Integer passedExams;
    private Integer failedExams;

    private Double passRate;
    private Double previousExamPercentage;
    private Double latestExamPercentage;
    private Double percentageChange;
    private List<ChapterPerformanceDTO> chapterPerformance;
}
