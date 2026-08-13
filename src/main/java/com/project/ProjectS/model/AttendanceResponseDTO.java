package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class AttendanceResponseDTO {

    private Long attendanceId;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}