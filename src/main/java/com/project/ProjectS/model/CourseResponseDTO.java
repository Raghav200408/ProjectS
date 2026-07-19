package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CourseResponseDTO {

    private Long courseId;

    private Long branchId;
    private String branchName;

    private String name;

    private Boolean activeRow;
    private Integer rowStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}