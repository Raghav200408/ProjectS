package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {


    boolean existsByNameAndBranch(
            String name,
            Branch branch
    );


    Optional<Course> findByName(String name);


    Optional<Course> findByNameAndBranch(
            String name,
            Branch branch
    );

}