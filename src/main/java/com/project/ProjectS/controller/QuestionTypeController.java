package com.project.ProjectS.controller;

import com.project.ProjectS.model.QuestionTypeRequestDTO;
import com.project.ProjectS.model.QuestionTypeResponseDTO;
import com.project.ProjectS.service.QuestionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-types")
public class QuestionTypeController {

    private final QuestionTypeService service;

    @Autowired
    public QuestionTypeController(QuestionTypeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> create(
            @RequestBody QuestionTypeRequestDTO request) {

        String response =
                service.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<QuestionTypeResponseDTO>> getAll() {

        List<QuestionTypeResponseDTO> response =
                service.getAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionTypeResponseDTO> getById(
            @PathVariable Long id) {

        QuestionTypeResponseDTO response =
                service.getById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @RequestBody QuestionTypeRequestDTO request) {

        String response =
                service.update(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        String response =
                service.delete(id);

        return ResponseEntity.ok(response);
    }
}
