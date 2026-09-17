package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MockExamSubmitRequestDTO {

    private Long userId;

    private List<ExamQuestionAnswerDTO> answers;

    // Elapsed seconds on the exam's own countdown when it was submitted.
    // Purely informational (shown on the review screen) - never affects scoring.
    private Integer timeTakenSeconds;
}
