package com.project.ProjectS.repository;

import com.project.ProjectS.entity.QuestionCategory;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, Long> {

    boolean existsByCourseAndChapterAndName(
            Course course,
            Chapter chapter,
            String name
    );

    Optional<QuestionCategory> findByCourseAndChapterAndName(
            Course course,
            Chapter chapter,
            String name
    );
}