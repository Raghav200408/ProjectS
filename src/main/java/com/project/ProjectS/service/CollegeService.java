package com.project.ProjectS.service;

import com.project.ProjectS.entity.College;
import com.project.ProjectS.model.CollegeRequestDTO;
import com.project.ProjectS.model.CollegeResponseDTO;
import com.project.ProjectS.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollegeService {

    private static final Logger logger =
            LogManager.getLogger(CollegeService.class);

    @Autowired
    private CollegeRepository repository;

    public String create(CollegeRequestDTO request) {

        logger.info("Creating college with name: {}", request.getInstituteName());
        if (repository.existsByInstituteName(request.getInstituteName())) {
            logger.warn("College already exists with name: {}", request.getInstituteName());
            throw new RuntimeException("College already exists");
        }

        College entity = new College();

        entity.setInstituteName(request.getInstituteName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());

        repository.save(entity);

        logger.info("College created successfully. College ID: {}", entity.getCollegeId());
        return "College created successfully";
    }

    public List<CollegeResponseDTO> getAll() {

        logger.info("Fetching all colleges.");

        List<CollegeResponseDTO> colleges = repository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        logger.info("Fetched {} colleges.", colleges.size());

        return colleges;
    }

    public College getById(Long id) {

        logger.info("Fetching college with ID: {}", id);

        College entity = repository.findById(id)
                .orElseThrow(() -> {

                    logger.warn("College not found with ID: {}", id);

                    return new RuntimeException("College not found");
                });

        logger.info("College fetched successfully with ID: {}", id);

        return entity;
    }

    public String update(Long id, CollegeRequestDTO request) {

        logger.info("Updating college with ID: {}", id);

        College entity = repository.findById(id)
                .orElseThrow(() -> {

                    logger.warn("College not found with ID: {}", id);

                    return new RuntimeException("College not found");
                });

        entity.setInstituteName(request.getInstituteName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());

        repository.save(entity);

        logger.info("College updated successfully with ID: {}", id);

        return "College updated successfully";
    }

    public String delete(Long id) {

        logger.info("Deleting college with ID: {}", id);

        College entity = repository.findById(id)
                .orElseThrow(() -> {

                    logger.warn("College not found with ID: {}", id);

                    return new RuntimeException("College not found");
                });

        repository.delete(entity);

        logger.info("College deleted successfully with ID: {}", id);

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