package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionCategoryRequestDTO {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Chapter ID is required")
    private Long chapterId;

    @NotBlank(message = "Category name is required")
    private String name;

    private Boolean activeRow;

}