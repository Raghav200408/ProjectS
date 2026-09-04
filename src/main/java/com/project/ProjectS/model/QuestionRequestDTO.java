package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Getter
@Setter
public class QuestionRequestDTO {

    private Long courseId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private Long chapterId;

    private Long topicId;
    private Long questionTypeId;

    private String questionText;


    private List<QuestionAttributeRequestDTO> questionAttributes;
}
