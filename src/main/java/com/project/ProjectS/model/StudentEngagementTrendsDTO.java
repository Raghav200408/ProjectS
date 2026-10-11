package com.project.ProjectS.model;

import java.util.List;

public record StudentEngagementTrendsDTO(
        long totalStudentCount,
        long activeStudentCount,
        List<StudentDailyEngagementDTO> daily,
        StudentConsistencyDTO consistency
) {}
