package com.project.ProjectS.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionFilterRequestDTO {

    @NotNull(message = "Course is required")
    private Long courseId;

    @NotEmpty(message = "Please select at least one chapter")
    private List<Long> chapterIds;

}