package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class QuestionResponseDTO {

    private Long questionId;


    private Long courseId;
    private String courseName;

    private Long subjectId;
    private String subjectName;


    private Long chapterId;
    private String chapterName;


    private Long topicId;
    private String topicName;

    private Long questionTypeId;
    private String questionType;


    private String questionText;


    private List<QuestionAttributeResponseDTO> questionAttributes;

    private Boolean activeRow;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
