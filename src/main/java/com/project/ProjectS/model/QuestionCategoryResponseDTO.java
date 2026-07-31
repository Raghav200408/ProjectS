package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuestionCategoryResponseDTO {

    private Long categoryId;

    private Long courseId;
    private String courseName;

    private Long chapterId;
    private String chapterName;

    private String name;

    private Boolean activeRow;
    private Integer rowStatus;
    private Integer orderOf;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}