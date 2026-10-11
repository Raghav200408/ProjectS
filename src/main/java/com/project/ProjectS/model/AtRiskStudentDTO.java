package com.project.ProjectS.model;

import java.time.LocalDate;

public record AtRiskStudentDTO(String name, LocalDate lastActiveDate, long activeDaysLast30Days) {}
