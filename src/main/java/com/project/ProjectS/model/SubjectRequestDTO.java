package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectRequestDTO {
    @NotNull(message = "Course ID is required")
    private Long courseId;
    @NotBlank(message = "Subject name is required")
    private String subjectName;
    private Boolean activeRow;
}
