package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequestDTO {

    @NotNull(message = "Branch Id is required")
    private Long branchId;

    @NotBlank(message = "Course name is required")
    private String name;

}