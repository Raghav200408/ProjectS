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

    private String studentName;

    private String section;
    private Long collegeId;
    private String collegeName;
    private Long branchId;
    private String branchName;
    private Long courseId;
    private String courseName;

    private LocalDate attendanceDate;

    private String day;

    private LocalTime inTime;

    private LocalTime outTime;

    private String status;

    private String statusDescription;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
