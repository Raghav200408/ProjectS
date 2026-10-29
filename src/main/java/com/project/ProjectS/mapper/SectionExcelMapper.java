package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.CourseAssignmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SectionExcelMapper implements ExcelRowMapper<Section> {
    @Autowired
    public SectionExcelMapper(CourseRepository courseRepository, BranchRepository branchRepository, CourseAssignmentRepository assignmentRepository) {
        this.courseRepository = courseRepository;
        this.branchRepository = branchRepository;
        this.assignmentRepository = assignmentRepository;
    }

    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final CourseAssignmentRepository assignmentRepository;

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

        // Resolve the shared catalog course through its branch assignment.
        var matches = assignmentRepository.findActiveForScope(
                        branch.getCollege().getCollegeId(), branch.getBranchId())
                .stream()
                .map(assignment -> assignment.getCourse())
                .filter(course -> course.getName().equalsIgnoreCase(finalCourseName))
                .distinct()
                .toList();
        if (matches.size() != 1) {
            throw new RuntimeException(matches.isEmpty()
                    ? "Course not assigned to branch: " + finalCourseName
                    : "Multiple catalog courses match " + finalCourseName + "; use a unique course name");
        }
        Course course = matches.get(0);

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
