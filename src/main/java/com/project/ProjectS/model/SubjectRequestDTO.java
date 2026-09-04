package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectRequestDTO {

    private String subjectName;

    private Long courseId;

    private Boolean activeRow;

    private Integer rowStatus;
}