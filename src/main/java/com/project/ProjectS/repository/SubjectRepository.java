package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsBySubjectNameAndCourse_CourseId(
            String subjectName,
            Long courseId
    );

    boolean existsBySubjectNameAndCourse_CourseIdAndSubjectIdNot(
            String subjectName,
            Long courseId,
            Long subjectId
    );

    List<Subject> findByActiveRowTrue();

    List<Subject> findByCourse_CourseId(Long courseId);
}