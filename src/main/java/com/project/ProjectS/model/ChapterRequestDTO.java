package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChapterRequestDTO {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Chapter name is required")
    private String name;


}