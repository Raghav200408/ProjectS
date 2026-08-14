package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CourseExcelMapper implements ExcelRowMapper<Course> {
    @Autowired
    public CourseExcelMapper(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    private final BranchRepository branchRepository;

    @Override
    public Course map(Map<String, String> row) {

        String branchName = row.get("branch_name");
        String courseName = row.get("course_name");

        // Remove extra spaces
        if (branchName != null) {
            branchName = branchName.trim();
        }

        if (courseName != null) {
            courseName = courseName.trim();
        }

        // Validate Branch Name
        if (branchName == null || branchName.isBlank()) {
            throw new RuntimeException("Branch Name is required");
        }

        // Validate Course Name
        if (courseName == null || courseName.isBlank()) {
            throw new RuntimeException("Course Name is required");
        }

        // Find Branch
        List<Branch> branches =
                branchRepository.findByBranchName(branchName);

        if (branches.isEmpty()) {
            throw new RuntimeException(
                    "Branch not found : " + branchName
            );
        }

        Branch branch = branches.get(0);

        // Create Course
        Course course = new Course();

        // Set Branch
        course.setBranch(branch);

        // Set College from Branch
        course.setCollege(branch.getCollege());

        // Set Course Name
        course.setName(courseName);

        return course;
    }
}
