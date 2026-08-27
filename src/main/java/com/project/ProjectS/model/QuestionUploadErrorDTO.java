package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUploadErrorDTO {

    private int rowNumber;

    private String questionText;

    private String headerName;

    private String attributeName;

    private String errorMessage;
}