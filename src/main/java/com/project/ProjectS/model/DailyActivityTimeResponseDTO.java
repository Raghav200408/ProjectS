package com.project.ProjectS.model;

import java.time.LocalDate;

public record DailyActivityTimeResponseDTO(
        LocalDate date,
        long activeSeconds
) {
}
