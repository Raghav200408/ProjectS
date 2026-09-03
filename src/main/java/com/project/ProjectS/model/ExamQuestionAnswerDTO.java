package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExamQuestionAnswerDTO {

    private Long questionId;

    private String questionType;

    private List<ExamAnswerDTO> answers;
}
