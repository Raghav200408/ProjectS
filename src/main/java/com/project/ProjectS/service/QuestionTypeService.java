package com.project.ProjectS.service;

import com.project.ProjectS.entity.QuestionType;
import com.project.ProjectS.model.QuestionTypeRequestDTO;
import com.project.ProjectS.model.QuestionTypeResponseDTO;
import com.project.ProjectS.repository.QuestionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionTypeService {

    private final QuestionTypeRepository repository;

    @Autowired
    public QuestionTypeService(QuestionTypeRepository repository) {
        this.repository = repository;
    }

    public String create(QuestionTypeRequestDTO request) {

        String questionType =
                validateAndNormalizeQuestionType(request);

        if (repository.existsByQuestionType(questionType)) {
            throw new RuntimeException("Question Type already exists");
        }

        QuestionType entity =
                new QuestionType();

        entity.setQuestionType(questionType);

        repository.save(entity);

        return "Question Type created successfully";
    }

    public List<QuestionTypeResponseDTO> getAll() {

        List<QuestionType> questionTypes =
                repository.findAll();

        List<QuestionTypeResponseDTO> response =
                new ArrayList<>();

        for (QuestionType questionType : questionTypes) {
            response.add(convertToResponse(questionType));
        }

        return response;
    }

    public QuestionTypeResponseDTO getById(Long id) {

        QuestionType questionType =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question Type not found"
                                )
                        );

        return convertToResponse(questionType);
    }

    public String update(
            Long id,
            QuestionTypeRequestDTO request) {

        QuestionType entity =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question Type not found"
                                )
                        );

        String questionType =
                validateAndNormalizeQuestionType(request);

        QuestionType existing =
                repository.findByQuestionType(questionType)
                        .orElse(null);

        if (existing != null &&
                !existing.getQuestionTypeId().equals(id)) {
            throw new RuntimeException("Question Type already exists");
        }

        entity.setQuestionType(questionType);

        repository.save(entity);

        return "Question Type updated successfully";
    }

    public String delete(Long id) {

        QuestionType entity =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question Type not found"
                                )
                        );

        repository.delete(entity);

        return "Question Type deleted successfully";
    }

    private QuestionTypeResponseDTO convertToResponse(
            QuestionType questionType) {

        QuestionTypeResponseDTO dto =
                new QuestionTypeResponseDTO();

        dto.setQuestionTypeId(
                questionType.getQuestionTypeId()
        );

        dto.setQuestionType(
                questionType.getQuestionType()
        );

        dto.setCreatedAt(
                questionType.getCreatedAt()
        );

        return dto;
    }

    private String validateAndNormalizeQuestionType(
            QuestionTypeRequestDTO request) {

        if (request == null ||
                request.getQuestionType() == null ||
                request.getQuestionType().trim().isEmpty()) {
            throw new RuntimeException("Question Type is required");
        }

        return request.getQuestionType().trim();
    }
}
