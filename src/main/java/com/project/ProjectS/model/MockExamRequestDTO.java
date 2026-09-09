package com.project.ProjectS.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MockExamRequestDTO {

    @NotBlank(message = "Mock exam name is required")
    private String mockExamName;

    @NotNull(message = "Course Id is required")
    private Long courseId;

    @NotEmpty(message = "Please select at least one chapter")
    private List<Long> chapterIds;

    @NotNull(message = "Pass percentage is required")
    @Min(value = 0, message = "Pass percentage must be at least 0")
    @Max(value = 100, message = "Pass percentage must be at most 100")
    private Integer passPercentage;
}
