package com.project.ProjectS.controller;

import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
@RestController
@RequestMapping("/api/section")
public class SectionController {

    private static final Logger logger =
            LogManager.getLogger(SectionController.class);

    @Autowired
    private SectionService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody SectionRequestDTO request) {

        logger.info("Received request to create section.");

        String response = service.create(request);

        logger.info("Create section request completed successfully.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<SectionResponseDTO> getAll() {

        logger.info("Received request to fetch all sections.");

        List<SectionResponseDTO> sections = service.getAll();

        logger.info("Fetched {} sections successfully.", sections.size());

        return sections;
    }

    @GetMapping("/{id}")
    public SectionResponseDTO getById(@PathVariable Long id) {

        logger.info("Received request to fetch section with ID: {}", id);

        SectionResponseDTO section = service.getById(id);

        logger.info("Section fetched successfully with ID: {}", id);

        return section;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody SectionRequestDTO request) {

        logger.info("Received request to update section with ID: {}", id);

        String response = service.update(id, request);

        logger.info("Section updated successfully with ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        logger.info("Received request to delete section with ID: {}", id);

        String response = service.delete(id);

        logger.info("Section deleted successfully with ID: {}", id);

        return response;
    }
}