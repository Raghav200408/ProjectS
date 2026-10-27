package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository
        extends JpaRepository<Question, Long> {
    List<Question> findByCourse_CourseIdAndChapter_ChapterIdInAndActiveRowTrue(
            Long courseId,
            List<Long> chapterIds
    );


    List<Question> findByActiveRowTrue();
   
    List<Question>
    findByCourse_CourseIdAndChapter_ChapterIdAndTopic_TopicIdAndActiveRowTrue(
            Long courseId,
            Long chapterId,
            Long topicId
    );
}