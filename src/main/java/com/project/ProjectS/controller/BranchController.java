package com.project.ProjectS.controller;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.model.BranchRequestDTO;
import com.project.ProjectS.model.BranchResponseDTO;
import com.project.ProjectS.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branch")
public class BranchController {

    @Autowired
    private BranchService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody BranchRequestDTO request) {

        String response = service.create(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<BranchResponseDTO> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public Branch getById(@PathVariable Long id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequestDTO request) {

        String response = service.update(id, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        return service.delete(id);
    }
}