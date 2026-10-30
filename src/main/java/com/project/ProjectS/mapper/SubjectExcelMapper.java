package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SubjectExcelMapper {
    public Subject map(Map<String, String> row, Course course) {
        String rowNumber = row.getOrDefault("_excel_row_number", "?");
        String courseName = required(row, "course_name", rowNumber);
        String subjectName = required(row, "subject_name", rowNumber);
        if (!course.getName().trim().equalsIgnoreCase(courseName)) {
            throw new IllegalArgumentException("Row " + rowNumber
                    + ": course_name must match the selected course: " + course.getName());
        }
        if (subjectName.length() > 255) {
            throw new IllegalArgumentException("Row " + rowNumber + ": subject_name must be at most 255 characters");
        }
        String active = row.getOrDefault("active_row", "");
        active = active == null ? "" : active.trim();
        if (!active.isEmpty() && !active.equalsIgnoreCase("true") && !active.equalsIgnoreCase("false")
                && !active.equals("1") && !active.equals("0")) {
            throw new IllegalArgumentException("Row " + rowNumber + ": active_row must be true, false, 1 or 0");
        }
        Subject subject = new Subject();
        subject.setCourse(course);
        subject.setSubjectName(subjectName);
        subject.setActiveRow(active.isEmpty() || active.equalsIgnoreCase("true") || active.equals("1"));
        return subject;
    }

    private String required(Map<String, String> row, String field, String rowNumber) {
        String value = row.get(field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Row " + rowNumber + ": " + field + " is required");
        }
        return value.trim();
    }
}
