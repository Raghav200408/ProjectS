package com.project.ProjectS.service;

import com.project.ProjectS.model.DashboardResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SectionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final Logger logger =
            LogManager.getLogger(DashboardService.class);

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    public DashboardResponseDTO getDashboard() {

        logger.info("Fetching dashboard details.");

        DashboardResponseDTO response = new DashboardResponseDTO();

        response.setTotalColleges(collegeRepository.count());
        response.setTotalBranches(branchRepository.count());
        response.setTotalCourses(courseRepository.count());
        response.setTotalSections(sectionRepository.count());

        logger.info("Dashboard details fetched successfully.");

        return response;
    }
}