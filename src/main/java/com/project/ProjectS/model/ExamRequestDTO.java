package com.project.ProjectS.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ExamRequestDTO {

    @NotBlank(message = "Exam name is required")
    private String examName;

    @NotNull(message = "College Id is required")
    private Long collegeId;

    @NotNull(message = "Branch Id is required")
    private Long branchId;

    @NotNull(message = "Course Id is required")
    private Long courseId;

    @NotNull(message = "Section Id is required")
    private Long sectionId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @NotEmpty(message = "Please select at least one question")
    private List<Long> questionIds;
}