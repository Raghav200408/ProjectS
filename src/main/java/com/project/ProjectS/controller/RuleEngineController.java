package com.project.ProjectS.controller;

import com.project.ProjectS.entity.RuleEngine;
import com.project.ProjectS.model.RuleEngineRequestDTO;
import com.project.ProjectS.model.RuleEngineResponseDTO;
import com.project.ProjectS.service.RuleEngineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rule-engines")
public class RuleEngineController {

    @Autowired
    private RuleEngineService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody RuleEngineRequestDTO request) {

        String response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RuleEngineResponseDTO>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleEngineResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody RuleEngineRequestDTO request) {

        String response = service.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        return ResponseEntity.ok(service.delete(id));
    }
}
