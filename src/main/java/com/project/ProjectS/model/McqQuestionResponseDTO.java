package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class McqQuestionResponseDTO {

    private Long questionId;

    // Main question details
    private String questionText;

    private Long courseId;
    private String courseName;

    private Long chapterId;
    private String chapterName;

    private Long categoryId;
    private String categoryName;

    // MCQ configuration
    private String questionType;

    private Double marks;

    // MCQ options
    private List<McqOptionDTO> options;
}