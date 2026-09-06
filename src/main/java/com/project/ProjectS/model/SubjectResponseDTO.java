package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class SubjectResponseDTO {
    private Long subjectId;
    private String subjectName;
    private Long courseId;
    private String courseName;
    private Boolean activeRow;
    private Integer rowStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
