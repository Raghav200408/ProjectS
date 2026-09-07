package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class McqQuestionRequestDTO {

    private Long courseId;

    private Long chapterId;

    private Long topicId;

    private String questionText;

    private Long questionTypeId;

    // Backward compatibility for existing imports/clients. New clients use the ID.
    private String questionType;

    private Double marks;

    private List<McqOptionDTO> options;
}
