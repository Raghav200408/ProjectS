package com.project.ProjectS.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.ProjectS.entity.College;
import com.project.ProjectS.mapper.CollegeMapper;
import com.project.ProjectS.repository.CollegeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository repository;
    private final CollegeMapper mapper;

    // Create
    public com.project.ProjectS.model.CollegeResponseDTO saveCollege(com.project.ProjectS.model.CollegeRequestDTO request) {

        College college = mapper.toEntity(request);

        College savedCollege = repository.save(college);

        return mapper.toResponse(savedCollege);
    }

    // Get All
    public List<com.project.ProjectS.model.CollegeResponseDTO> getAllColleges() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get By Id
    public com.project.ProjectS.model.CollegeResponseDTO getCollegeById(Long id) {

        College college = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("College not found with ID : " + id));

        return mapper.toResponse(college);
    }

    // Update
    public com.project.ProjectS.model.CollegeResponseDTO updateCollege(Long id, com.project.ProjectS.model.CollegeRequestDTO request) {

        College existingCollege = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("College not found with ID : " + id));

        existingCollege.setInstituteName(request.getInstituteName());
        existingCollege.setAddress(request.getAddress());
        existingCollege.setPhoneNumber(request.getPhoneNumber());
        existingCollege.setEmail(request.getEmail());
        existingCollege.setActiveRow(request.getActiveRow());

        College updatedCollege = repository.save(existingCollege);

        return mapper.toResponse(updatedCollege);
    }

    // Delete
    public String deleteCollege(Long id) {

        College college = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("College not found with ID : " + id));

        repository.delete(college);

        return "College deleted successfully.";
    }
}