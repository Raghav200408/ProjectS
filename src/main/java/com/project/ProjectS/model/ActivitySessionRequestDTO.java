package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;

public record ActivitySessionRequestDTO(
        @NotBlank String sessionKey,
        Long chapterId,
        Long topicId,
        Long questionId
) {
}
