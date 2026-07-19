package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionRequestDTO {

    @NotNull(message = "Course Id is required")
    private Long courseId;

    @NotBlank(message = "Section name is required")
    private String sectionName;

    @NotBlank(message = "Description is required")
    private String description;

}