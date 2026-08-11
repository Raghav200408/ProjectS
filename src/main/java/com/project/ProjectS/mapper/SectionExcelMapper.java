package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SectionExcelMapper implements ExcelRowMapper<Section> {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public Section map(Map<String, String> row) {

        String branchName = row.get("branch_name");
        String courseName = row.get("course_name");

        // Remove extra spaces
        if (branchName != null) {
            branchName = branchName.trim();
        }

        if (courseName != null) {
            courseName = courseName.trim();
        }

        // Convert Excel branch code to database branch name
        if ("CSE".equalsIgnoreCase(branchName)) {
            branchName = "Computer Science Engineering";
        } else if ("AI&DS".equalsIgnoreCase(branchName)
                || "AIDS".equalsIgnoreCase(branchName)) {
            branchName = "Artificial Intelligence and Data Science";
        } else if ("IT".equalsIgnoreCase(branchName)) {
            branchName = "Information Technology";
        } else if ("ECE".equalsIgnoreCase(branchName)) {
            branchName = "Electronics and Communication Engineering";
        } else if ("ME".equalsIgnoreCase(branchName)) {
            branchName = "Mechanical Engineering";
        } else if ("CE".equalsIgnoreCase(branchName)) {
            branchName = "Civil Engineering";
        }

        // Create final variables for lambda
        final String finalBranchName = branchName;
        final String finalCourseName = courseName;

        // Find Branch
        Branch branch = branchRepository
                .findByBranchName(finalBranchName)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Branch not found : " + finalBranchName
                        )
                );

        // Find Course using Branch + Course Name
        Course course = courseRepository
                .findByNameAndBranch(finalCourseName, branch)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found : " + finalCourseName
                        )
                );

        // Create Section
        Section section = new Section();

        // Set College from Branch
        section.setCollege(branch.getCollege());

        // Set Branch
        section.setBranch(branch);

        // Set Course
        section.setCourse(course);

        // Set Section Name
        section.setSectionName(
                row.get("section_name")
        );

        // Set Description
        section.setDescription(
                row.get("description")
        );

        return section;
    }
}