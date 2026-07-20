package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamRequestDTO {

    @NotNull(message = "Chapter Id is Required")
    private Long chapterId;

    @NotNull(message = "Category Id is required")
    private Long categoryId;

    @NotBlank(message = "Question code is required")
    @Size(max = 30, message = "Question code must not exceed 30 characters")
    private String questionCode;

    @NotBlank(message = "Exam name is Requires")
    @Size(max = 255, message = "Exam name must not exceed 255 characters")
    private String name;
}
