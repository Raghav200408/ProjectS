package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChapterPerformanceDTO {
    private Long chapterId;
    private String chapterName;
    private Double accuracyPercentage;
    private Integer attemptedExams;
}
