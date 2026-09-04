package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {


    boolean existsByName(String name);


    boolean existsByNameAndCourse(
            String name,
            Course course
    );


    Optional<Chapter> findByName(String name);


    Optional<Chapter> findByNameAndCourse(
            String name,
            Course course
    );

    boolean existsByNameAndSubject(String name, Subject subject);

}
