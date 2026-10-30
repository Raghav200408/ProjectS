package com.project.ProjectS.controller;

import com.project.ProjectS.model.SubjectRequestDTO;
import com.project.ProjectS.model.SubjectResponseDTO;
import com.project.ProjectS.service.SubjectService;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.processor.SubjectExcelProcessor;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private static final Logger log = LogManager.getLogger(SubjectController.class);

    private final SubjectService service;
    private final ExcelUploadService excelUploadService;
    private final SubjectExcelProcessor subjectExcelProcessor;

    public SubjectController(SubjectService service, ExcelUploadService excelUploadService,
                             SubjectExcelProcessor subjectExcelProcessor) {
        this.service = service;
        this.excelUploadService = excelUploadService;
        this.subjectExcelProcessor = subjectExcelProcessor;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadSubjects(@RequestParam("file") MultipartFile file,
                                                @RequestParam("courseId") Long courseId) {
        try {
            return ResponseEntity.ok(subjectExcelProcessor.process(excelUploadService.readExcel(file), courseId));
        } catch (Exception error) {
            log.error("Subject Excel upload failed", error);
            return ResponseEntity.badRequest().body("Subject Excel upload failed: " + error.getMessage());
        }
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
