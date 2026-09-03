package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamSubmitResponseDTO {

    private Long examId;

    private Long userId;

    private Double totalMarks;

    private Double percentage;
}
