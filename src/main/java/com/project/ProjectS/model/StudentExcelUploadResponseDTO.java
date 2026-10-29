package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StudentExcelUploadResponseDTO {
    private int totalRows;
    private int studentsUploaded;
    private int skippedRows;
    private boolean success;
    private String message;
    private List<StudentExcelUploadErrorDTO> errors = new ArrayList<>();
}
