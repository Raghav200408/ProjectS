package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionExcelUploadResponseDTO {

    private int totalRows;

    private int uploadedQuestions;

    private int createdAttributes;

    private int skippedRows;

    private int failedQuestions;

    private boolean success;

    private String message;

    private List<QuestionUploadErrorDTO> errors =
            new ArrayList<>();

}