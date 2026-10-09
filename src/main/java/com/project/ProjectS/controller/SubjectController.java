package com.project.ProjectS.controller;

import com.project.ProjectS.model.SubjectRequestDTO;
import com.project.ProjectS.model.SubjectResponseDTO;
import com.project.ProjectS.service.SubjectService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private static final Logger log = LogManager.getLogger(SubjectController.class);

    private final SubjectService service;

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubjectResponseDTO> create(
            @Valid @RequestBody SubjectRequestDTO request) {
        log.info("Creating subject: name={}", request.getSubjectName());
        SubjectResponseDTO response = service.create(request);
        log.info("Subject created successfully: id={}", response.getSubjectId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAll() {
        log.info("Fetching all subjects");
        List<SubjectResponseDTO> response = service.getAll();
        log.info("Fetched {} subjects", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> getById(@PathVariable Long id) {
        log.info("Fetching subject by id={}", id);
        SubjectResponseDTO response = service.getById(id);
        log.info("Subject fetched successfully: id={}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequestDTO request) {
        log.info("Updating subject: id={} name={}", id, request.getSubjectName());
        SubjectResponseDTO response = service.update(id, request);
        log.info("Subject updated successfully: id={}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting subject: id={}", id);
        service.delete(id);
        log.info("Subject deleted successfully: id={}", id);
        return ResponseEntity.noContent().build();
    }
}
