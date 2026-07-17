package com.project.ProjectS.model;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDTO {

    private Long courseId;

    private Long branchId;
    private String branchName;

    private String name;
    private String rowStatus;
    private Boolean activeRow;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}