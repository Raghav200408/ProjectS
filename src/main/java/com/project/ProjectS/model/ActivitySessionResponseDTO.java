package com.project.ProjectS.model;

public record ActivitySessionResponseDTO(
        String sessionKey,
        String status,
        long totalActiveSeconds
) {
}