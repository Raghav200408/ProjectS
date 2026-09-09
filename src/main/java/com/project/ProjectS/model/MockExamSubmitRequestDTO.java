package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MockExamSubmitRequestDTO {

    private Long userId;

    private List<ExamQuestionAnswerDTO> answers;
}
