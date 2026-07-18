package com.project.ProjectS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.BranchRequestDTO;
import com.project.ProjectS.model.BranchResponseDTO;
import com.project.ProjectS.service.BranchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/branch")
public class BranchController {

    private final BranchService service;

    public BranchController(BranchService service) {
        this.service = service;
    }

    @PostMapping
    public BranchResponseDTO saveBranch(@RequestBody BranchRequestDTO request) {
        return service.saveBranch(request);
    }

    @GetMapping
    public List<BranchResponseDTO> getAllBranches() {
        return service.getAllBranches();
    }

    @GetMapping("/{id}")
    public BranchResponseDTO getBranchById(@PathVariable Long id) {
        return service.getBranchById(id);
    }

    @PutMapping("/{id}")
    public BranchResponseDTO updateBranch(@PathVariable Long id,
                                          @RequestBody BranchRequestDTO request) {
        return service.updateBranch(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteBranch(@PathVariable Long id) {
        return service.deleteBranch(id);
    }

}