package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionTypeRequestDTO {

    @NotBlank(message = "Question Type is required")
    private String questionType;
}
