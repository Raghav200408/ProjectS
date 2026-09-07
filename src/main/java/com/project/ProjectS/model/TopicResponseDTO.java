package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TopicResponseDTO {

    private Long topicId;

    private Long courseId;
    private String courseName;

    private Long subjectId;
    private String subjectName;

    private Long chapterId;
    private String chapterName;

    private String name;

    private Boolean activeRow;
    private Integer rowStatus;
    private Integer orderOf;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
