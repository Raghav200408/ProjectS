package com.project.ProjectS.controller;

import com.project.ProjectS.dto.TableNameRequestDTO;
import com.project.ProjectS.dto.TableNameResponseDTO;
import com.project.ProjectS.service.TableNameServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table-names")
public class TableNameController {

    @Autowired
    private TableNameServiceImpl service;

    @PostMapping
    public TableNameResponseDTO create(@Valid @RequestBody TableNameRequestDTO request) {
        return service.create(request);
    }

    @GetMapping
    public List<TableNameResponseDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public TableNameResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public TableNameResponseDTO update(@PathVariable Long id,
                                       @Valid @RequestBody TableNameRequestDTO request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return service.delete(id);
    }
}