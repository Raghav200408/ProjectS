package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuestionTypeResponseDTO {

    private Long questionTypeId;

    private String questionType;

    private LocalDateTime createdAt;
}
