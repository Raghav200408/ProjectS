package com.project.ProjectS.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.CollegeDTO;
import com.project.ProjectS.repository.CollegeDAO;

@Service
public class CollegeService {

    @Autowired
    private CollegeDAO collegeDAO;

    // Save College
    public CollegeDTO saveCollege(CollegeDTO college) {
        return collegeDAO.save(college);
    }

    // Get All Colleges
    public Iterable<CollegeDTO> getAllColleges() {
        return collegeDAO.findAll();
    }

    // Get College By Id
    public CollegeDTO getCollegeById(Long id) {
        return collegeDAO.findById(id).orElse(null);
    }

    // Delete College
    public void deleteCollege(Long id) {
        collegeDAO.deleteById(id);
    }

    // Update College
    public CollegeDTO updateCollege(CollegeDTO college) {
        return collegeDAO.save(college);
    }
}