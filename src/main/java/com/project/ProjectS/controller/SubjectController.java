package com.project.ProjectS.controller;


import com.project.ProjectS.model.SubjectRequestDTO;
import com.project.ProjectS.model.SubjectResponseDTO;
import com.project.ProjectS.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<String> create(
            @RequestBody SubjectRequestDTO request) {

        String response = subjectService.create(request);

        return ResponseEntity.ok(response);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAll() {

        List<SubjectResponseDTO> response =
                subjectService.getAll();

        return ResponseEntity.ok(response);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> getById(
            @PathVariable Long id) {

        SubjectResponseDTO response =
                subjectService.getById(id);

        return ResponseEntity.ok(response);
    }

    // GET ACTIVE SUBJECTS
    @GetMapping("/active")
    public ResponseEntity<List<SubjectResponseDTO>> getActiveSubjects() {

        List<SubjectResponseDTO> response =
                subjectService.getActiveSubjects();

        return ResponseEntity.ok(response);
    }

    // GET SUBJECTS BY COURSE
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<SubjectResponseDTO>> getByCourseId(
            @PathVariable Long courseId) {

        List<SubjectResponseDTO> response =
                subjectService.getByCourseId(courseId);

        return ResponseEntity.ok(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @RequestBody SubjectRequestDTO request) {

        String response =
                subjectService.update(id, request);

        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        String response =
                subjectService.delete(id);

        return ResponseEntity.ok(response);
    }
}
