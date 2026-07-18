package com.project.ProjectS.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionRequestDTO {

    private Long courseId;

    private String sectionName;

    private String description;

    private Boolean activeRow;

}