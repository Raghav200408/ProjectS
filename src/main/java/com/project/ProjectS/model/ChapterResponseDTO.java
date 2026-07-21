package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChapterResponseDTO {

    private Long chapterId;

    private Long courseId;
    private String courseName;

    private String name;

    private Boolean activeRow;
    private Integer rowStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}