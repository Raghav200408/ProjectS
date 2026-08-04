package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamQuestionResponseDTO {

    private Long examQuestionId;

    private Long questionId;

    private String questionText;

    private Long courseId;
    private String courseName;

    private Long chapterId;
    private String chapterName;

    private Long categoryId;
    private String categoryName;
}