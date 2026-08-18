package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ExamResponseDTO {

    private Long examId;

    private String examName;

    private Long collegeId;

    private Long branchId;

    private Long courseId;

    private String courseName;

    private Long sectionId;

    private List<Long> chapterIds;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean activeRow;

    private Integer rowStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}