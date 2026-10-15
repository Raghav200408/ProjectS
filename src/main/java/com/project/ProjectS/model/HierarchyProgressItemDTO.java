package com.project.ProjectS.model;

import java.math.BigDecimal;

/** Current question completion, separate from historical practice performance. */
public record HierarchyProgressItemDTO(Long id, long totalQuestions,
        long completedQuestions, BigDecimal completionPercentage) {
}
