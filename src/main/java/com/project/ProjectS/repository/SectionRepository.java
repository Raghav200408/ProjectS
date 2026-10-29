package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Section;
import com.project.ProjectS.entity.Course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByCollege_CollegeId(Long collegeId);

    List<Section> findByBranch_BranchId(Long branchId);

    boolean existsBySectionNameAndCourse_CourseId(
            String sectionName,
            Long courseId
    );

    boolean existsBySectionNameAndCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
            String sectionName, Long courseId, Long collegeId, Long branchId);

    boolean existsBySectionNameAndCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchIdAndSectionIdNot(
            String sectionName, Long courseId, Long collegeId, Long branchId, Long sectionId);

    boolean existsBySectionNameAndCourse(
            String sectionName,
            Course course
    );


    Optional<Section> findBySectionNameAndCourse(
            String sectionName,
            Course course
    );

    Optional<Section> findBySectionNameAndCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
            String sectionName, Long courseId, Long collegeId, Long branchId);

}
