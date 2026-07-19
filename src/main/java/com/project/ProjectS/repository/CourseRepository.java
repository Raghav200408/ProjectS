package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByName(String name);

    Optional<Course> findByName(String name);

}