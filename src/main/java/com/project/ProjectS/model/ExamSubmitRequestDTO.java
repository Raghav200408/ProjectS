package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExamSubmitRequestDTO {

    private Long userId;

    private List<ExamQuestionAnswerDTO> answers;
}
