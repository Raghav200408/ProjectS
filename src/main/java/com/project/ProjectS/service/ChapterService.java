package com.project.ProjectS.service;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.model.ChapterRequestDTO;
import com.project.ProjectS.model.ChapterResponseDTO;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CourseRepository courseRepository;

    public String create(ChapterRequestDTO request) {

        if (chapterRepository.existsByName(request.getName())) {
            throw new RuntimeException("Chapter already exists");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Chapter entity = new Chapter();

        entity.setCourse(course);
        entity.setName(request.getName());

        chapterRepository.save(entity);

        return "Chapter created successfully";
    }

    public List<ChapterResponseDTO> getAll() {

        return chapterRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public ChapterResponseDTO getById(Long id) {

        Chapter entity = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        return convert(entity);
    }

    public String update(Long id, ChapterRequestDTO request) {

        Chapter entity = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        entity.setCourse(course);
        entity.setName(request.getName());

        chapterRepository.save(entity);

        return "Chapter updated successfully";
    }

    public String delete(Long id) {

        Chapter entity = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        chapterRepository.delete(entity);

        return "Chapter deleted successfully";
    }

    private ChapterResponseDTO convert(Chapter entity) {

        ChapterResponseDTO dto = new ChapterResponseDTO();

        dto.setChapterId(entity.getChapterId());

        dto.setCourseId(entity.getCourse().getCourseId());
        dto.setCourseName(entity.getCourse().getName());

        dto.setName(entity.getName());

        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;

    }
}