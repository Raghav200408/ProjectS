package com.project.ProjectS.controller;

import com.project.ProjectS.model.TableAttributeRequestDTO;
import com.project.ProjectS.model.TableAttributeResponseDTO;
import com.project.ProjectS.service.TableAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table-attributes")
public class TableAttributeController {

    @Autowired
    private TableAttributeService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody TableAttributeRequestDTO request){

        String response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TableAttributeResponseDTO>> getAll(){

        List<TableAttributeResponseDTO> response = service.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableAttributeResponseDTO> getById(@PathVariable Long id){

        TableAttributeResponseDTO response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody TableAttributeRequestDTO request){

        String response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){

        String response = service.delete(id);
        return ResponseEntity.ok(response);
    }
}