package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionRequestDTO {

    private Long courseId;

    private Long chapterId;

    private Long categoryId;
    private Long questionTypeId;

    private String questionText;


    private List<QuestionAttributeRequestDTO> questionAttributes;
}