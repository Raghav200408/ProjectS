package com.project.ProjectS.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CourseAssignmentBulkRequestDTO {
    @NotNull
    @Valid
    private List<CourseAssignmentRequestDTO> assignments;
}
