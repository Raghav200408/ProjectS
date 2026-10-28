package com.project.ProjectS.repository;

import com.project.ProjectS.entity.CourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {
    List<CourseAssignment> findByCourse_CourseId(Long courseId);

    List<CourseAssignment> findByBranch_BranchId(Long branchId);

    List<CourseAssignment> findByCourse_CourseIdAndActiveRowTrue(Long courseId);

    @Query("""
        select distinct a from CourseAssignment a
        where a.activeRow = true
          and a.college.collegeId = :collegeId
          and a.branch is null
        """)
    List<CourseAssignment> findActiveCollegeWide(@Param("collegeId") Long collegeId);

    @Query("""
        select distinct a from CourseAssignment a
        where a.activeRow = true
          and a.college.collegeId = :collegeId
          and (:branchId is null or a.branch is null or a.branch.branchId = :branchId)
        """)
    List<CourseAssignment> findActiveForScope(@Param("collegeId") Long collegeId,
                                               @Param("branchId") Long branchId);

    boolean existsByCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
            Long courseId, Long collegeId, Long branchId);

    boolean existsByCourse_CourseIdAndCollege_CollegeIdAndBranchIsNull(
            Long courseId, Long collegeId);

    void deleteByCourse_CourseId(Long courseId);
}
