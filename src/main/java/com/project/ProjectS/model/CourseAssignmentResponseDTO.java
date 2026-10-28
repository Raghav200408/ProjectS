package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseAssignmentResponseDTO {
    private Long assignmentId;
    private Long collegeId;
    private String collegeName;
    private Long branchId;
    private String branchName;
    private Boolean activeRow;
}
