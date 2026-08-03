package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SectionResponseDTO {

    private Long sectionId;

    private Long collegeId;
    private String collegeName;

    private Long branchId;
    private String branchName;

    private Long courseId;
    private String courseName;

    private String sectionName;
    private String description;

    private Boolean activeRow;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}