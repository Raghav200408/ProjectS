package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AttendanceRequestDTO {

    private String studentId;

    private String rollNo;

    private String studentName;

    private String section;

    private LocalDate attendanceDate;

    private String day;

    private LocalTime inTime;

    private LocalTime outTime;

    private String status;

    private String statusDescription;
}