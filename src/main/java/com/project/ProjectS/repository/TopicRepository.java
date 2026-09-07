package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import com.project.ProjectS.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsByCourseAndChapterAndName(
            Course course,
            Chapter chapter,
            String name
    );

    Optional<Topic> findByCourseAndChapterAndName(
            Course course,
            Chapter chapter,
            String name
    );

    boolean existsByCourseAndSubjectAndChapterAndName(
            Course course,
            Subject subject,
            Chapter chapter,
            String name
    );

    Optional<Topic> findByCourseAndSubjectAndChapterAndName(
            Course course,
            Subject subject,
            Chapter chapter,
            String name
    );
}
