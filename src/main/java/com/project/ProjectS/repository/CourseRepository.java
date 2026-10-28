package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {


    boolean existsByNameAndBranch(
            String name,
            Branch branch
    );


    List<Course> findByNameIgnoreCase(String name);

    Optional<Course> findByNameAndBranch(
            String name,
            Branch branch
    );

    List<Course> findDistinctByAssignments_College_CollegeIdAndAssignments_ActiveRowTrue(Long collegeId);

    List<Course> findDistinctByAssignments_College_CollegeIdAndAssignments_Branch_BranchIdAndAssignments_ActiveRowTrue(
            Long collegeId, Long branchId);

}