package com.project.ProjectS.model;

import java.util.List;

public record StudentConsistencyDTO(
        long lowConsistencyStudentCount,
        long regularStudentCount,
        long highConsistencyStudentCount,
        long atRiskStudentCount,
        List<AtRiskStudentDTO> atRiskStudents
) {}
