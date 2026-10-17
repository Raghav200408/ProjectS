package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnouncementRequestDTO(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 500) String message) {}
