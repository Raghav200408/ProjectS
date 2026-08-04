package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentExamResponseDTO {

    private Long examId;

    private String examName;

    private QuestionResponseDTO question;
}