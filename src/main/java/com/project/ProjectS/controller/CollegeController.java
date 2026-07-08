package com.project.ProjectS.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.CollegeDTO;
import com.project.ProjectS.service.CollegeService;

@RestController
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    // Save College
    @PostMapping("/saveCollege")
    public CollegeDTO saveCollege(@RequestBody CollegeDTO college) {
        return collegeService.saveCollege(college);
    }

    // Get All Colleges
    @GetMapping("/getAllColleges")
    public Iterable<CollegeDTO> getAllColleges() {
        return collegeService.getAllColleges();
    }

    // Get College By Id
    @GetMapping("/getCollegeById/{id}")
    public CollegeDTO getCollegeById(@PathVariable Long id) {
        return collegeService.getCollegeById(id);
    }

    // Update College
    @PutMapping("/updateCollege")
    public CollegeDTO updateCollege(@RequestBody CollegeDTO college) {
        return collegeService.updateCollege(college);
    }

    // Delete College
    @DeleteMapping("/deleteCollege/{id}")
    public String deleteCollege(@PathVariable Long id) {
        collegeService.deleteCollege(id);
        return "College deleted successfully";
    }
}