package com.project.ProjectS.controller;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.model.TableHeaderRequestDTO;
import com.project.ProjectS.service.TableHeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table-headers")
public class TableHeaderController {

    @Autowired
    private TableHeaderService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody TableHeaderRequestDTO request) {

        String response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TableHeader>> getAll() {

        List<TableHeader> response = service.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableHeader> getById(@PathVariable Long id) {

        TableHeader response = service.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody TableHeaderRequestDTO request) {

        String response = service.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        String response = service.delete(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}