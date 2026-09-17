package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full payload for the "Exam Review" screen: the attempt's summary plus a
 * per-question breakdown built from the snapshot captured at submit time.
 */
@Getter
@Setter
public class ExamReviewResponseDTO {

    private Long resultId;

    private Long examId;

    private String examName;

    private String courseName;

    private Double totalMarks;

    private Double maximumMarks;

    private Double percentage;

    private Integer timeTakenSeconds;

    private LocalDateTime completedAt;

    private List<ExamReviewQuestionDTO> questions;
}
