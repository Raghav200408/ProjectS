package com.project.ProjectS.controller;

import com.project.ProjectS.entity.ExamQuestion;
import com.project.ProjectS.model.ExamQuestionRequestDTO;
import com.project.ProjectS.model.ExamQuestionResponseDTO;
import com.project.ProjectS.service.ExamQuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-question")
public class ExamQuestionController {

    @Autowired
    private ExamQuestionService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody ExamQuestionRequestDTO request) {

        String response = service.create(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ExamQuestionResponseDTO> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public ExamQuestion getById(@PathVariable Long id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody ExamQuestionRequestDTO request) {

        String response = service.update(id, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        return service.delete(id);
    }
}