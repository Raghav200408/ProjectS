package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamParticipationDTO {
    private Long examId;
    private String examName;
    private String sectionName;
    private Integer assignedStudents;
    private Integer attemptedStudents;
    private Integer notAttemptedStudents;
    private Double participationRate;
}
