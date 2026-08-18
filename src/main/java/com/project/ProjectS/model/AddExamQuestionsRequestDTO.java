package com.project.ProjectS.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddExamQuestionsRequestDTO {

    @NotEmpty(message = "Please select at least one question")
    private List<Long> questionIds;
}