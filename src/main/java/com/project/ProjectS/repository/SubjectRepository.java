package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsBySubjectNameAndCourse(String subjectName, Course course);

    Optional<Subject> findBySubjectNameAndCourse(String subjectName, Course course);
}
