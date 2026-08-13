package com.project.ProjectS.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceExcelUploadResponseDTO {

    private String status;

    private String message;

    private int totalRecords;

    private int savedRecords;
}