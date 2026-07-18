package com.project.ProjectS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.service.SectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/section")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService service;

    @PostMapping
    public SectionResponseDTO saveSection(@RequestBody SectionRequestDTO request) {
        return service.saveSection(request);
    }

    @GetMapping
    public List<SectionResponseDTO> getAllSections() {
        return service.getAllSections();
    }

    @GetMapping("/{id}")
    public SectionResponseDTO getSectionById(@PathVariable Long id) {
        return service.getSectionById(id);
    }

    @PutMapping("/{id}")
    public SectionResponseDTO updateSection(@PathVariable Long id,
                                            @RequestBody SectionRequestDTO request) {
        return service.updateSection(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteSection(@PathVariable Long id) {
        return service.deleteSection(id);
    }
}