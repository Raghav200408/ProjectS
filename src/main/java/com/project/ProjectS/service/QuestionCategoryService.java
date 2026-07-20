package com.project.ProjectS.service;

import com.project.ProjectS.entity.QuestionCategory;
import com.project.ProjectS.model.QuestionCategoryRequestDTO;
import com.project.ProjectS.model.QuestionCategoryResponseDTO;
import com.project.ProjectS.repository.QuestionCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionCategoryService {

    @Autowired
    private QuestionCategoryRepository repository;

    public String create(QuestionCategoryRequestDTO request) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Question Category already exists");
        }

        QuestionCategory category = new QuestionCategory();
        category.setName(request.getName());

        repository.save(category);

        return "Question Category created successfully";
    }

    public List<QuestionCategoryResponseDTO> getAll() {

        List<QuestionCategory> categories = repository.findAll();
        List<QuestionCategoryResponseDTO> response = new ArrayList<>();

        for (QuestionCategory category : categories) {

            QuestionCategoryResponseDTO dto = new QuestionCategoryResponseDTO();

            dto.setCategoryId(category.getCategoryId());
            dto.setName(category.getName());
            dto.setActiveRow(category.getActiveRow());
            dto.setRowStatus(category.getRowStatus());
            dto.setOrderOf(category.getOrderOf());
            dto.setCreatedAt(category.getCreatedAt());
            dto.setUpdatedAt(category.getUpdatedAt());

            response.add(dto);
        }

        return response;
    }

    public QuestionCategoryResponseDTO getById(Long id) {

        QuestionCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question Category not found"));

        QuestionCategoryResponseDTO dto = new QuestionCategoryResponseDTO();

        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setActiveRow(category.getActiveRow());
        dto.setRowStatus(category.getRowStatus());
        dto.setOrderOf(category.getOrderOf());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        return dto;
    }

    public String update(Long id, QuestionCategoryRequestDTO request) {

        QuestionCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question Category not found"));

        category.setName(request.getName());

        repository.save(category);

        return "Question Category updated successfully";
    }

    public String delete(Long id) {

        QuestionCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question Category not found"));

        repository.delete(category);

        return "Question Category deleted successfully";
    }
}