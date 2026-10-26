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
            if (courseName == null || courseName.isBlank()) throw new IllegalArgumentException("Course Name is required");
            if (row.get("branch_id") == null || row.get("branch_id").isBlank()) {
                Course global = new Course(); global.setName(courseName); return global;
            }
            Branch selected = branchRepository.findById(Long.valueOf(row.get("branch_id").trim()))
                    .orElseThrow(() -> new IllegalArgumentException("Branch ID not found"));
            Course mapped = new Course(); mapped.setName(courseName); mapped.setBranch(selected); mapped.setCollege(selected.getCollege()); return mapped;
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

        if (branches.size() != 1) throw new IllegalArgumentException("Branch name is ambiguous; supply branch_id without branch_name");
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
