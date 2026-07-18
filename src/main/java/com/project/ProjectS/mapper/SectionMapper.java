package com.project.ProjectS.mapper;

import org.springframework.stereotype.Component;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;

@Component
public class SectionMapper {

    public Section toEntity(SectionRequestDTO dto, Course course) {

        return Section.builder()
                .course(course)
                .sectionName(dto.getSectionName())
                .description(dto.getDescription())
                .activeRow(dto.getActiveRow())
                .build();
    }

    public SectionResponseDTO toResponseDTO(Section section) {

        return SectionResponseDTO.builder()
                .sectionId(section.getSectionId())
                .courseId(section.getCourse().getCourseId())
                .courseName(section.getCourse().getName())
                .sectionName(section.getSectionName())
                .description(section.getDescription())
                .activeRow(section.getActiveRow())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}