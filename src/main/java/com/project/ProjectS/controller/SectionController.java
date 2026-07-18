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

@RestController
@RequestMapping("/api/section")
public class SectionController {

    @Autowired
    private SectionService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody SectionRequestDTO request) {

        String response = service.create(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<SectionResponseDTO> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public SectionResponseDTO getById(@PathVariable Long id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody SectionRequestDTO request) {

        String response = service.update(id, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        return service.delete(id);
    }
}