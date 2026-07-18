package com.project.ProjectS.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.mapper.SectionMapper;
import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SectionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final SectionMapper sectionMapper;

    // Create
    public SectionResponseDTO saveSection(SectionRequestDTO request) {

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Section section = sectionMapper.toEntity(request, course);

        Section saved = sectionRepository.save(section);

        return sectionMapper.toResponseDTO(saved);
    }

    // Get All
    public List<SectionResponseDTO> getAllSections() {

        return sectionRepository.findAll()
                .stream()
                .map(sectionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get By Id
    public SectionResponseDTO getSectionById(Long id) {

        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        return sectionMapper.toResponseDTO(section);
    }

    // Update
    public SectionResponseDTO updateSection(Long id, SectionRequestDTO request) {

        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        section.setCourse(course);
        section.setSectionName(request.getSectionName());
        section.setDescription(request.getDescription());
        section.setActiveRow(request.getActiveRow());

        Section updated = sectionRepository.save(section);

        return sectionMapper.toResponseDTO(updated);
    }

    // Delete
    public String deleteSection(Long id) {

        if (!sectionRepository.existsById(id)) {
            throw new RuntimeException("Section not found");
        }

        sectionRepository.deleteById(id);

        return "Section deleted successfully.";
    }
}