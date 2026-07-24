package com.project.ProjectS.service;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {

    private static final Logger logger =
            LogManager.getLogger(SectionService.class);

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    public String create(SectionRequestDTO request) {

        logger.info("Creating section with name: {}", request.getSectionName());

        if (sectionRepository.existsBySectionName(request.getSectionName())) {
            logger.warn("Section already exists with name: {}", request.getSectionName());
            throw new RuntimeException("Section already exists");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", request.getCourseId());
                    return new RuntimeException("Course not found");
                });

        Section entity = new Section();

        entity.setCourse(course);
        entity.setSectionName(request.getSectionName());
        entity.setDescription(request.getDescription());

        sectionRepository.save(entity);

        logger.info("Section created successfully with name: {}", request.getSectionName());

        return "Section created successfully";
    }

    public List<SectionResponseDTO> getAll() {

        logger.info("Fetching all sections.");

        List<SectionResponseDTO> sections = sectionRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        logger.info("Fetched {} sections.", sections.size());

        return sections;
    }

    public SectionResponseDTO getById(Long id) {

        logger.info("Fetching section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });

        logger.info("Section fetched successfully with ID: {}", id);

        return convert(entity);
    }
    public String update(Long id, SectionRequestDTO request) {

        logger.info("Updating section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", request.getCourseId());
                    return new RuntimeException("Course not found");
                });

        entity.setCourse(course);
        entity.setSectionName(request.getSectionName());
        entity.setDescription(request.getDescription());

        sectionRepository.save(entity);

        logger.info("Section updated successfully with ID: {}", id);

        return "Section updated successfully";
    }

    public String delete(Long id) {

        logger.info("Deleting section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });

        sectionRepository.delete(entity);

        logger.info("Section deleted successfully with ID: {}", id);

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