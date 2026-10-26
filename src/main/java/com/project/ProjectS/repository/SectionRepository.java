package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Section;
import com.project.ProjectS.entity.Course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    boolean existsBySectionNameAndCourse_CourseIdAndBranch_BranchId(String name, Long courseId, Long branchId);
    boolean existsBySectionNameAndCourse_CourseIdAndBranch_BranchIdAndSectionIdNot(String name, Long courseId, Long branchId, Long sectionId);

    boolean existsBySectionNameAndCourse_CourseId(
            String sectionName,
            Long courseId
    );

    boolean existsBySectionNameAndCourse(
            String sectionName,
            Course course
    );


    Optional<Section> findBySectionNameAndCourse(
            String sectionName,
            Course course
    );


}
