package com.project.ProjectS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.service.CollegeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService service;

    // Create College
    @PostMapping
    public com.project.ProjectS.model.CollegeResponseDTO saveCollege(@RequestBody com.project.ProjectS.model                .CollegeRequestDTO request) {
        return service.saveCollege(request);
    }

    // Get All Colleges
    @GetMapping
    public List<com.project.ProjectS.model.CollegeResponseDTO> getAllColleges() {
        return service.getAllColleges();
    }

    // Get College By Id
    @GetMapping("/{id}")
    public com.project.ProjectS.model.CollegeResponseDTO getCollegeById(@PathVariable Long id) {
        return service.getCollegeById(id);
    }

    // Update College
    @PutMapping("/{id}")
    public com.project.ProjectS.model.CollegeResponseDTO updateCollege(@PathVariable Long id,
                                                                                @RequestBody com.project.ProjectS.model.CollegeRequestDTO request) {
        return service.updateCollege(id, request);
    }

    // Delete College
    @DeleteMapping("/{id}")
    public String deleteCollege(@PathVariable Long id) {
        return service.deleteCollege(id);
    }
}