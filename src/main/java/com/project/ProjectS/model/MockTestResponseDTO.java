package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MockTestResponseDTO {
    private Long courseId;
    private int questionCount;
    private List<McqQuestionResponseDTO> questions;
}
