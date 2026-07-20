package com.project.ProjectS.controller;

import com.project.ProjectS.entity.Exam;
import com.project.ProjectS.model.ExamRequestDTO;
import com.project.ProjectS.model.ExamResponseDTO;
import com.project.ProjectS.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody ExamRequestDTO request) {

        String response = service.create(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ExamResponseDTO> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public Exam getById(@PathVariable Long id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody ExamRequestDTO request) {

        String response = service.update(id, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        return service.delete(id);
    }
}