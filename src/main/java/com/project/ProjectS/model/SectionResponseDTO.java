package com.project.ProjectS.model;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionResponseDTO {

    private Long sectionId;

    private Long courseId;

    private String courseName;

    private String sectionName;

    private String description;

    private Boolean activeRow;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}