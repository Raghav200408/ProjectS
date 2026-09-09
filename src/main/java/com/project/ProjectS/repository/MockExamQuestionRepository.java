package com.project.ProjectS.repository;

import com.project.ProjectS.entity.MockExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockExamQuestionRepository
        extends JpaRepository<MockExamQuestion, Long> {

    List<MockExamQuestion> findByMockExam_MockExamId(Long mockExamId);

    boolean existsByMockExam_MockExamIdAndQuestion_QuestionId(
            Long mockExamId,
            Long questionId
    );

    void deleteByMockExam_MockExamIdAndQuestion_QuestionId(
            Long mockExamId,
            Long questionId
    );
}
