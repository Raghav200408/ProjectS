package com.project.ProjectS.controller;

import com.project.ProjectS.model.QuestionCategoryRequestDTO;
import com.project.ProjectS.model.QuestionCategoryResponseDTO;
import com.project.ProjectS.service.QuestionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-categories")
public class QuestionCategoryController {

    @Autowired
    private QuestionCategoryService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody QuestionCategoryRequestDTO request) {

        String response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<QuestionCategoryResponseDTO>> getAll() {

        List<QuestionCategoryResponseDTO> response = service.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionCategoryResponseDTO> getById(@PathVariable Long id) {

        QuestionCategoryResponseDTO response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody QuestionCategoryRequestDTO request) {

        String response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        String response = service.delete(id);
        return ResponseEntity.ok(response);
    }
}