package com.project.ProjectS.model;

public record CourseAssignmentDTO(Long assignmentId, Long courseId, Long collegeId,
        String collegeName, Long branchId, String branchName, Boolean activeRow) {}
