package com.project.ProjectS.processor;


import com.project.ProjectS.entity.Course;
import com.project.ProjectS.mapper.CourseExcelMapper;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.CourseAssignmentRepository;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import com.project.ProjectS.entity.CourseAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class CourseExcelProcessor {
    @Autowired
    public CourseExcelProcessor(CourseExcelMapper courseExcelMapper, CourseRepository courseRepository, CourseAssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.courseExcelMapper = courseExcelMapper;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    private final CourseExcelMapper courseExcelMapper;
    private final CourseRepository courseRepository;
    private final CourseAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;



    @org.springframework.transaction.annotation.Transactional
    public void process(List<Map<String, String>> excelData, Authentication authentication) {


        User actor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Admin account not found"));
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();

        for(Map<String,String> row : excelData){


            if(row.values()
                    .stream()
                    .allMatch(String::isBlank)){

                continue;
            }



            Course rowCourse = courseExcelMapper.map(row);
            var branch = rowCourse.getBranch();
            Long collegeId = branch.getCollege().getCollegeId();
            Long branchId = branch.getBranchId();
            if ("COLLEGE_ADMIN".equalsIgnoreCase(role)
                    && (actor.getCollege() == null || !actor.getCollege().getCollegeId().equals(collegeId))) {
                throw new AccessDeniedException("Course upload includes a branch outside your college");
            }
            if ("BRANCH_ADMIN".equalsIgnoreCase(role)
                    && (actor.getBranch() == null || !actor.getBranch().getBranchId().equals(branchId))) {
                throw new AccessDeniedException("Course upload includes a branch outside your branch scope");
            }
            if (!"SUPER_ADMIN".equalsIgnoreCase(role)
                    && !"COLLEGE_ADMIN".equalsIgnoreCase(role)
                    && !"BRANCH_ADMIN".equalsIgnoreCase(role)) {
                throw new AccessDeniedException("Admin role required");
            }

            var scopedMatches = assignmentRepository.findActiveForScope(collegeId, branchId).stream()
                    .map(CourseAssignment::getCourse)
                    .filter(course -> course.getName().equalsIgnoreCase(rowCourse.getName()))
                    .distinct()
                    .toList();
            if (scopedMatches.size() > 1) {
                throw new IllegalArgumentException("Multiple catalog courses named " + rowCourse.getName()
                        + " are already available to branch " + branch.getBranchName());
            }

            Course course;
            if (!scopedMatches.isEmpty()) {
                course = scopedMatches.get(0);
            } else {
                var sameName = courseRepository.findByNameIgnoreCase(rowCourse.getName());
                if (sameName.size() > 1) {
                    throw new IllegalArgumentException("Course name is ambiguous in the catalog: " + rowCourse.getName()
                            + ". Assign the intended course from Course Management instead.");
                }
                course = sameName.isEmpty() ? new Course() : sameName.get(0);
                if (course.getCourseId() == null) {
                    course.setName(rowCourse.getName());
                    course.setActiveRow(true);
                    course.setRowStatus(1);
                    course = courseRepository.save(course);
                }

                CourseAssignment assignment = assignmentRepository
                        .findByCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
                                course.getCourseId(), collegeId, branchId)
                        .orElseGet(CourseAssignment::new);
                assignment.setCourse(course);
                assignment.setCollege(branch.getCollege());
                assignment.setBranch(branch);
                assignment.setActiveRow(true);
                assignmentRepository.save(assignment);
            }
        }

    }

}
