package com.project.ProjectS.model;

import java.time.LocalDate;

public record StudentDailyEngagementDTO(
        LocalDate date,
        long activeSeconds,
        long activeStudentCount
) {}
