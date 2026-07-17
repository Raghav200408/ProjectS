package com.project.ProjectS.mapper;

import org.springframework.stereotype.Component;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.model.CourseRequestDTO;
import com.project.ProjectS.model.CourseResponseDTO;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequestDTO dto, Branch branch) {

        return Course.builder()
                .branch(branch)
                .name(dto.getName())
                .rowStatus(dto.getRowStatus())
                .activeRow(dto.getActiveRow())
                .build();
    }

    public CourseResponseDTO toResponseDTO(Course course) {

        return CourseResponseDTO.builder()
                .courseId(course.getCourseId())
                .branchId(course.getBranch().getBranchId())
                .branchName(course.getBranch().getBranchName())
                .name(course.getName())
                .rowStatus(course.getRowStatus())
                .activeRow(course.getActiveRow())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}