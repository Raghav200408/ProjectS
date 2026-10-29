package com.project.ProjectS.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseAssignmentRequestDTO {
    @NotNull(message = "College ID is required")
    private Long collegeId;

    // Null means the course is available to every branch in the college.
    private Long branchId;

    private Boolean activeRow = true;
}
