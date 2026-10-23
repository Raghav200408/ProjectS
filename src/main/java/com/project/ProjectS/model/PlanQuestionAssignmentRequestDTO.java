package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PlanQuestionAssignmentRequestDTO {
    private Integer questionLimit;
    private boolean allCourseQuestions;
    private List<Long> questionIds;
}
