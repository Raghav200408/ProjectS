package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CourseResponseDTO {

    private Long collegeId;
    private String collegeName;

    private Long courseId;

    private Long branchId;
    private String branchName;

    private List<CourseAssignmentResponseDTO> assignments;

    private String name;

    private Boolean activeRow;
    private Integer rowStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}