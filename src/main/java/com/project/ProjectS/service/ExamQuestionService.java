package com.project.ProjectS.service;

import com.project.ProjectS.entity.Exam;
import com.project.ProjectS.entity.ExamQuestion;
import com.project.ProjectS.model.ExamQuestionRequestDTO;
import com.project.ProjectS.model.ExamQuestionResponseDTO;
import com.project.ProjectS.repository.ExamQuestionRepository;
import com.project.ProjectS.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamQuestionService {

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Autowired
    private ExamRepository examRepository;

    public String create(ExamQuestionRequestDTO request) {

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        ExamQuestion entity = new ExamQuestion();

        entity.setExam(exam);
        entity.setHeaderId(request.getHeaderId());
        entity.setAttributeId(request.getAttributeId());
        entity.setTransactionDate(request.getTransactionDate());
        entity.setAmount(request.getAmount());
        entity.setAmount2(request.getAmount2());
        entity.setExternalFile(request.getExternalFile());
        entity.setNote(request.getNote());

        examQuestionRepository.save(entity);

        return "Exam Question created successfully";
    }

    public List<ExamQuestionResponseDTO> getAll() {

        return examQuestionRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public ExamQuestion getById(Long id) {

        return examQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam Question not found"));
    }

    public String update(Long id, ExamQuestionRequestDTO request) {

        ExamQuestion entity = examQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam Question not found"));

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        entity.setExam(exam);
        entity.setHeaderId(request.getHeaderId());
        entity.setAttributeId(request.getAttributeId());
        entity.setTransactionDate(request.getTransactionDate());
        entity.setAmount(request.getAmount());
        entity.setAmount2(request.getAmount2());
        entity.setExternalFile(request.getExternalFile());
        entity.setNote(request.getNote());

        examQuestionRepository.save(entity);

        return "Exam Question updated successfully";
    }

    public String delete(Long id) {

        ExamQuestion entity = examQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam Question not found"));

        examQuestionRepository.delete(entity);

        return "Exam Question deleted successfully";
    }

    private ExamQuestionResponseDTO convert(ExamQuestion entity) {

        ExamQuestionResponseDTO dto = new ExamQuestionResponseDTO();

        dto.setQuestionId(entity.getQuestionId());
        dto.setExamId(entity.getExam().getExamId());
        dto.setHeaderId(entity.getHeaderId());
        dto.setAttributeId(entity.getAttributeId());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setAmount(entity.getAmount());
        dto.setAmount2(entity.getAmount2());
        dto.setExternalFile(entity.getExternalFile());
        dto.setNote(entity.getNote());
        dto.setActiveRow(entity.getActiveRow());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}