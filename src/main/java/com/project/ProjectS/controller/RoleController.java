package com.project.ProjectS.controller;

import com.project.ProjectS.model.RoleResponseDTO;
import com.project.ProjectS.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService service;

    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> getAll() {

        List<RoleResponseDTO> response = service.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getById(@PathVariable Integer id) {

        RoleResponseDTO response = service.getById(id);
        return ResponseEntity.ok(response);
    }
}