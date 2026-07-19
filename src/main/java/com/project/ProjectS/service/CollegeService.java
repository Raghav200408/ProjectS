package com.project.ProjectS.service;

import com.project.ProjectS.entity.College;
import com.project.ProjectS.model.CollegeRequestDTO;
import com.project.ProjectS.model.CollegeResponseDTO;
import com.project.ProjectS.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollegeService {

    @Autowired
    private CollegeRepository repository;

    public String create(CollegeRequestDTO request) {

        if (repository.existsByInstituteName(request.getInstituteName())) {
            throw new RuntimeException("College already exists");
        }

        College entity = new College();

        entity.setInstituteName(request.getInstituteName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());

        repository.save(entity);

        return "College created successfully";
    }

    public List<CollegeResponseDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public College getById(Long id) {

        College entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("College not found"));

        return entity;
    }

    public String update(Long id, CollegeRequestDTO request) {

        College entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("College not found"));

        entity.setInstituteName(request.getInstituteName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());

        repository.save(entity);

        return "College updated successfully";
    }

    public String delete(Long id) {

        College entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("College not found"));

        repository.delete(entity);

        return "College deleted successfully";
    }

    private CollegeResponseDTO convert(College entity) {

        CollegeResponseDTO dto = new CollegeResponseDTO();

        dto.setCollegeId(entity.getCollegeId());
        dto.setInstituteName(entity.getInstituteName());
        dto.setAddress(entity.getAddress());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        dto.setActiveRow(entity.getActiveRow());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}