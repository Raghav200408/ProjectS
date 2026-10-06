package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// The chapter-by-chapter view of one exam attempt, for the Performance
// dashboard's trend drill-down: which chapters were on the paper, and how
// many marks this attempt scored in each one.
@Getter
@Setter
public class ExamChapterBreakdownResponseDTO {

    private Long resultId;
    private Long examId;
    private String examName;
    private Long studentId;
    private String studentName;

    private Double totalMarks;
    private Double maximumMarks;
    private Double percentage;

    private List<ChapterMarksDTO> chapters;
}
