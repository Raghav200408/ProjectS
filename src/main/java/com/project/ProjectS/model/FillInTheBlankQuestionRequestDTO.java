package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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

    private BigDecimal marks;

    private List<FillInTheBlankAnswerRequestDTO> answers;
}