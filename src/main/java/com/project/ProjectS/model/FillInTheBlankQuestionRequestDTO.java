package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FillInTheBlankQuestionRequestDTO {

    private Long courseId;

    private Long subjectId;

    private Long chapterId;

    private Long topicId;

    private Long questionTypeId;

    private String questionText;

    private List<FillInTheBlankAnswerRequestDTO> answers;
}