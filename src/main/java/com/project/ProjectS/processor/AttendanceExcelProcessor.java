package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Attendance;
import com.project.ProjectS.mapper.AttendanceExcelMapper;
import com.project.ProjectS.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AttendanceExcelProcessor implements ExcelProcessor {

    @Autowired
    private AttendanceExcelMapper attendanceMapper;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            // Skip completely empty rows
            if (row.values().stream().allMatch(String::isBlank)) {
                continue;
            }

            Attendance attendance = attendanceMapper.map(row);

            attendanceRepository.save(attendance);
        }
    }
}