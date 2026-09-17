package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One row in "my recent attempts" (Recent Activity on the dashboard / exam
 * hub). {@code examType} tells the frontend which review route and which
 * submit/service endpoints to use - "EXAM" or "MOCK_EXAM".
 */
@Getter
@Setter
public class ExamAttemptSummaryDTO {

    private Long resultId;

    private Long examId;

    private String examName;

    private String examType;

    private Double percentage;

    private LocalDateTime completedAt;
}
