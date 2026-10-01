package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;

public record ActivitySessionRequestDTO(@NotBlank String sessionKey) {
}
