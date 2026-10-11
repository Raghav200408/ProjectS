package com.project.ProjectS.model;

public record PeriodComparisonDTO(
        int days,
        long currentSeconds,
        long previousSeconds,
        long currentActiveDays,
        long previousActiveDays,
        long currentActiveStudentCount,
        long previousActiveStudentCount,
        long totalStudentCount
) {}
