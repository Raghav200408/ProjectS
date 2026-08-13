package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository
        extends JpaRepository<Question, Long> {
    List<Question> findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
            Long courseId,
            List<Long> chapterIds
    );

    Optional<Question> findByCourseAndChapterAndQuestionCategoryAndQuestionText(
            com.project.ProjectS.entity.Course course,
            com.project.ProjectS.entity.Chapter chapter,
            com.project.ProjectS.entity.QuestionCategory questionCategory,
            String questionText
    );

    List<Question> findByActiveRowTrue();
}