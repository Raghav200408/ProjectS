package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Role;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.RoleRepository;
import com.project.ProjectS.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserExcelMapper implements ExcelRowMapper<User> {

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User map(Map<String, String> row) {

        User user = new User();

        // =========================
        // Basic User Details
        // =========================

        String name = row.get("name");

        if (name == null || name.isBlank()) {
            throw new RuntimeException("Name is required");
        }

        user.setName(name.trim());

        user.setDesignation(
                getValue(row, "designation")
        );

        user.setAddress(
                getValue(row, "address")
        );

        user.setEmail(
                getValue(row, "email")
        );

        user.setPhoneNumber(
                getValue(row, "phone_number")
        );

        user.setPassword(
                getValue(row, "password")
        );

        // =========================
        // Student ID
        // =========================

        String studentId = row.get("student_id");

        if (studentId != null && !studentId.isBlank()) {
            try {
                user.setStudentCode(
                        Long.parseLong(studentId.trim())
                );
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        "Invalid Student ID: " + studentId
                );
            }
        }

        // =========================
        // Guardian Details
        // =========================

        user.setGuardianName(
                getValue(row, "guardian_name")
        );

        user.setGuardianPhoneNumber(
                getValue(row, "guardian_phone_number")
        );

        // =========================
        // Find College
        // =========================

        String collegeName = row.get("college_name");

        College college = null;

        if (collegeName != null && !collegeName.isBlank()) {

            List<College> colleges =
                    collegeRepository.findByInstituteName(
                            collegeName.trim()
                    );

            if (colleges.isEmpty()) {
                throw new RuntimeException(
                        "College not found: " + collegeName
                );
            }

            college = colleges.get(0);

            user.setCollege(college);
        }

        // =========================
        // Find Branch
        // =========================

        String branchName = row.get("branch_name");

        Branch branch = null;

        if (branchName != null && !branchName.isBlank()) {

            List<Branch> branches =
                    branchRepository.findByBranchName(
                            branchName.trim()
                    );

            if (branches.isEmpty()) {
                throw new RuntimeException(
                        "Branch not found: " + branchName
                );
            }

            branch = branches.get(0);

            user.setBranch(branch);
        }

        // =========================
// Find Course
// =========================

        String courseName = row.get("course_name");

        Course course = null;

        if (courseName != null && !courseName.isBlank()) {

            if (branch == null) {
                throw new RuntimeException(
                        "Branch Name is required to find Course: "
                                + courseName
                );
            }

            String selectedBranchName = branch.getBranchName();

            course = courseRepository
                    .findByNameAndBranch(
                            courseName.trim(),
                            branch
                    )
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Course not found: "
                                            + courseName
                                            + " for branch: "
                                            + selectedBranchName
                            )
                    );
        }
        // =========================
        // Find Section
        // =========================

        String sectionName = row.get("section_name");

        if (sectionName != null && !sectionName.isBlank()) {

            if (course == null) {
                throw new RuntimeException(
                        "Course Name is required to find Section: "
                                + sectionName
                );
            }

            Section section =
                    sectionRepository.findBySectionNameAndCourse(
                                    sectionName.trim(),
                                    course
                            )
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Section not found: "
                                                    + sectionName
                                                    + " for course: "
                                                    + courseName
                                    )
                            );

            user.setSection(section);
        }

        // =========================
        // Find Role
        // =========================

        String roleName = row.get("role");

        if (roleName == null || roleName.isBlank()) {
            throw new RuntimeException(
                    "Role is required"
            );
        }

        Role role =
                roleRepository.findByRoleName(
                                roleName.trim()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Role not found: " + roleName
                                )
                        );

        user.setRole(role);

        return user;
    }

    // =========================
    // Helper Method
    // =========================

    private String getValue(
            Map<String, String> row,
            String key) {

        String value = row.get(key);

        if (value == null) {
            return null;
        }

        value = value.trim();

        return value.isBlank() ? null : value;
    }
}