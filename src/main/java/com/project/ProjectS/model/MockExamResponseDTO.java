package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MockExamResponseDTO {

    private Long mockExamId;

    private String mockExamName;

    private Long courseId;

    private String courseName;

    private List<Long> chapterIds;

    private List<String> chapterNames;

    private Integer passPercentage;

    private Boolean activeRow;

    private Integer rowStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
