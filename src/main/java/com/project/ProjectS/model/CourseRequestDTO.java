package com.project.ProjectS.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequestDTO {

    private Long branchId;
    private String name;
    private String rowStatus;
    private Boolean activeRow;

}