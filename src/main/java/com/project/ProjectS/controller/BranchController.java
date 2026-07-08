package com.project.ProjectS.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.BranchDTO;
import com.project.ProjectS.service.BranchService;

import java.util.List;

@RestController
public class BranchController {

    @Autowired
    private BranchService branchService;

    // Save Branch
    @PostMapping("/saveBranch")
    public BranchDTO saveBranch(@RequestBody BranchDTO branch) {
        return branchService.saveBranch(branch);
    }

    // Get All Branches
    @GetMapping("/getAllBranches")
    public List<BranchDTO> getAllBranches() {
        return branchService.getAllBranches();
    }
    
    // Get Branch By Id
    @GetMapping("/getBranchById/{id}")
    public BranchDTO getBranchById(@PathVariable Long id) {
        return branchService.getBranchById(id);
    }

    // Update Branch
    @PutMapping("/updateBranch")
    public BranchDTO updateBranch(@RequestBody BranchDTO branch) {
        return branchService.updateBranch(branch);
    }

    // Delete Branch
    @DeleteMapping("/deleteBranch/{id}")
    public String deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return "Branch deleted successfully";
    }
}