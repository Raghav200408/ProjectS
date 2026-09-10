package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MockExamSubmitResponseDTO {

    private Long mockExamId;

    private Long userId;

    private Double totalMarks;

    private Double percentage;
}
