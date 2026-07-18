package com.project.ProjectS.service;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    public String create(SectionRequestDTO request) {

        if (sectionRepository.existsBySectionName(request.getSectionName())) {
            throw new RuntimeException("Section already exists");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Section entity = new Section();

        entity.setCourse(course);
        entity.setSectionName(request.getSectionName());
        entity.setDescription(request.getDescription());

        sectionRepository.save(entity);

        return "Section created successfully";
    }

    public List<SectionResponseDTO> getAll() {

        return sectionRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public SectionResponseDTO getById(Long id) {

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        return convert(entity);
    }

    public String update(Long id, SectionRequestDTO request) {

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        entity.setCourse(course);
        entity.setSectionName(request.getSectionName());
        entity.setDescription(request.getDescription());

        sectionRepository.save(entity);

        return "Section updated successfully";
    }

    public String delete(Long id) {

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        sectionRepository.delete(entity);

        return "Section deleted successfully";
    }

    private SectionResponseDTO convert(Section entity) {

        SectionResponseDTO dto = new SectionResponseDTO();

        dto.setSectionId(entity.getSectionId());

        dto.setCourseId(entity.getCourse().getCourseId());
        dto.setCourseName(entity.getCourse().getName());

        dto.setSectionName(entity.getSectionName());
        dto.setDescription(entity.getDescription());

        dto.setActiveRow(entity.getActiveRow());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}