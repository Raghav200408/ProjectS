package com.project.ProjectS.service;

import com.project.ProjectS.entity.Exam;
import com.project.ProjectS.model.ExamRequestDTO;
import com.project.ProjectS.model.ExamResponseDTO;
import com.project.ProjectS.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    public String create(ExamRequestDTO request) {

        if (examRepository.existsByQuestionCode(request.getQuestionCode())) {
            throw new RuntimeException("Question Code already exists");
        }

        Exam entity = new Exam();

        entity.setChapterId(request.getChapterId());
        entity.setCategoryId(request.getCategoryId());
        entity.setQuestionCode(request.getQuestionCode());
        entity.setName(request.getName());

        examRepository.save(entity);

        return "Exam created successfully";
    }

    public List<ExamResponseDTO> getAll() {

        return examRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public Exam getById(Long id) {

        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
    }

    public String update(Long id, ExamRequestDTO request) {

        Exam entity = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (!entity.getQuestionCode().equals(request.getQuestionCode())
                && examRepository.existsByQuestionCode(request.getQuestionCode())) {
            throw new RuntimeException("Question Code already exists");
        }

        entity.setChapterId(request.getChapterId());
        entity.setCategoryId(request.getCategoryId());
        entity.setQuestionCode(request.getQuestionCode());
        entity.setName(request.getName());

        examRepository.save(entity);

        return "Exam updated successfully";
    }

    public String delete(Long id) {

        Exam entity = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        examRepository.delete(entity);

        return "Exam deleted successfully";
    }

    private ExamResponseDTO convert(Exam entity) {

        ExamResponseDTO dto = new ExamResponseDTO();

        dto.setExamId(entity.getExamId());
        dto.setChapterId(entity.getChapterId());
        dto.setCategoryId(entity.getCategoryId());
        dto.setQuestionCode(entity.getQuestionCode());
        dto.setName(entity.getName());
        dto.setActiveRow(entity.getActiveRow());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}