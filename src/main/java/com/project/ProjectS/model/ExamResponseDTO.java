package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExamResponseDTO {

   private Long examId;

   private Long chapterId;

    private Long categoryId;

    private String questionCode;

    private String name;

    private Boolean activeRow;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
