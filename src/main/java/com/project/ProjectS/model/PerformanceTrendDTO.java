package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PerformanceTrendDTO {

    private Long examId;
    private String examName;
    private Double averagePercentage;
    private LocalDateTime examDate;

    // The one attempt behind this bar, only when the bar covers exactly one
    // student (a studentId filter, or a scope that only ever has one
    // attempt). Null when the bar averages several students, since there is
    // then no single attempt to open. The frontend uses this to enable the
    // "view attempt" / chapter-breakdown drill-down on a bar.
    private Long resultId;
}
