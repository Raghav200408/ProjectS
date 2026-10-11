package com.project.ProjectS.model;

import java.time.LocalDate;

public record ContentDailyActivityTimeResponseDTO(
        LocalDate date,
        String contentLevel,
        Long contentId,
        String contentName,
        long activeSeconds
) {
}
