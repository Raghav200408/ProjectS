package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequestDTO {

    private Long branchId;

    @NotBlank(message = "Course name is required")
    private String name;

    private Long collegeId;
    // Status fields
    private Boolean activeRow;

    private Integer rowStatus;

}
