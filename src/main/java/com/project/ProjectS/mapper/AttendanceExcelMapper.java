package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Attendance;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Component
public class AttendanceExcelMapper implements ExcelRowMapper<Attendance> {

    @Override
    public Attendance map(Map<String, String> row) {

        Attendance attendance = new Attendance();

        attendance.setStudentId(row.get("student_id"));
        attendance.setRollNo(row.get("roll_no"));
        attendance.setStudentName(row.get("student_name"));
        attendance.setSection(row.get("section"));

        String attendanceDate = row.get("attendance_date");

        if (attendanceDate != null && !attendanceDate.trim().isEmpty()) {
            attendance.setAttendanceDate(
                    LocalDate.parse(attendanceDate.trim())
            );
        }

        attendance.setDay(row.get("day"));

        String inTime = row.get("in_time");

        if (inTime != null && !inTime.trim().isEmpty()) {
            attendance.setInTime(
                    LocalTime.parse(inTime.trim())
            );
        }

        String outTime = row.get("out_time");

        if (outTime != null && !outTime.trim().isEmpty()) {
            attendance.setOutTime(
                    LocalTime.parse(outTime.trim())
            );
        }

        attendance.setStatus(row.get("status"));

        attendance.setStatusDescription(
                row.get("status_description")
        );

        return attendance;
    }
}