package com.project.ProjectS.model;

import java.util.List;

public record StudentLoginInactivityDTO(
        long totalStudentCount,
        long inactiveStudentCount,
        long untrackedStudentCount,
        List<StudentLastLoginDTO> students
) {}
