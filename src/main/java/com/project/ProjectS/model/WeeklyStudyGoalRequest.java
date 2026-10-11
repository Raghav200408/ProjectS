package com.project.ProjectS.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WeeklyStudyGoalRequest(
        @NotNull @DecimalMin("1.0") @DecimalMax("40.0") BigDecimal weeklyGoalHours
) {}
