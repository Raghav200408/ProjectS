package com.project.ProjectS.repository;

import com.project.ProjectS.entity.CourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {
    List<CourseAssignment> findByCourse_CourseId(Long courseId);
    Optional<CourseAssignment> findByCourse_CourseIdAndBranch_BranchId(Long courseId, Long branchId);
    boolean existsByCourse_CourseIdAndBranch_BranchIdAndActiveRowTrue(Long courseId, Long branchId);
    boolean existsByCourse_CourseIdAndBranch_College_CollegeIdAndActiveRowTrue(Long courseId, Long collegeId);
}
