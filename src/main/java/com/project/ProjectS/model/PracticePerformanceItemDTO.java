package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Practice metrics for one course / subject / chapter / topic.
 */
@Getter
@Setter
public class PracticePerformanceItemDTO {

    private Long id;
    private String name;

    // The level above (a topic's chapter, a chapter's subject, a subject's
    // course). Null for courses. Names repeat, so this tells them apart.
    private String parentName;

    // Units the students in scope could attempt: the per-student unit count
    // of this level times the number of students in scope.
    private long totalUnits;
    private long attemptedUnits;
    private long correctUnits;

    // attempted / total * 100, and correct / attempted * 100 - both worked
    // out from the summed counts, never by averaging per-question figures.
    private BigDecimal completionPercentage;
    private BigDecimal accuracyPercentage;
}
