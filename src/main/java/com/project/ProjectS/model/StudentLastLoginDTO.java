package com.project.ProjectS.model;

import java.time.LocalDateTime;

public record StudentLastLoginDTO(Long userId, String name, LocalDateTime lastLoginAt) {}
