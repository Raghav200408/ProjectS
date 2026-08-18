package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Attendance;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Component
public class AttendanceExcelMapper implements ExcelRowMapper<Attendance> {

    @Override
    public Attendance map(Map<String, String> row) {

        Attendance attendance = new Attendance();

        // Student details
        attendance.setStudentId(row.get("student_id"));
        attendance.setRollNo(row.get("roll_no"));
        attendance.setStudentName(row.get("student_name"));
        attendance.setSection(row.get("section"));

        // Attendance date
        String date = row.get("date");

        if (date != null && !date.trim().isEmpty()) {
            attendance.setAttendanceDate(
                    parseDate(date)
            );
        }

        // Day
        attendance.setDay(row.get("day"));

        // In Time
        String inTime = row.get("in_time");

        if (inTime != null && !inTime.trim().isEmpty()) {
            attendance.setInTime(
                    parseTime(inTime)
            );
        }

        // Out Time
        String outTime = row.get("out_time");

        if (outTime != null && !outTime.trim().isEmpty()) {
            attendance.setOutTime(
                    parseTime(outTime)
            );
        }

        // Status
        attendance.setStatus(row.get("status"));

        // Status Description
        attendance.setStatusDescription(
                row.get("status_description")
        );

        return attendance;
    }

    private LocalDate parseDate(String value) {

        value = value.trim();

        // Example:
        // 2026-08-01
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(value);
        }

        // Example:
        // 2026-08-01T00:00
        if (value.contains("T")) {
            return LocalDateTime
                    .parse(value)
                    .toLocalDate();
        }

        throw new IllegalArgumentException(
                "Invalid attendance date: " + value
        );
    }

    private LocalTime parseTime(String value) {

        value = value.trim();

        // Example:
        // 08:57
        if (value.matches("\\d{2}:\\d{2}")) {
            return LocalTime.parse(value);
        }

        // Example:
        // 08:57:00
        if (value.matches("\\d{2}:\\d{2}:\\d{2}")) {
            return LocalTime.parse(value);
        }

        // Example:
        // 1899-12-31T08:57
        if (value.contains("T")) {
            return LocalDateTime
                    .parse(value)
                    .toLocalTime();
        }

        throw new IllegalArgumentException(
                "Invalid attendance time: " + value
        );
    }
}