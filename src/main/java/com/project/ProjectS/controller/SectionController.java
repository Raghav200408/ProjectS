package com.project.ProjectS.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.SectionDTO;
import com.project.ProjectS.service.SectionService;

@RestController
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @PostMapping("/saveSection")
    public SectionDTO saveSection(@RequestBody SectionDTO section) {
        return sectionService.saveSection(section);
    }

    @GetMapping("/getAllSections")
    public List<SectionDTO> getAllSections() {
        return sectionService.getAllSections();
    }

    @GetMapping("/getSectionById/{id}")
    public SectionDTO getSectionById(@PathVariable Long id) {
        return sectionService.getSectionById(id);
    }

    @PutMapping("/updateSection")
    public SectionDTO updateSection(@RequestBody SectionDTO section) {
        return sectionService.updateSection(section);
    }

    @DeleteMapping("/deleteSection/{id}")
    public String deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return "Section deleted successfully";
    }
}