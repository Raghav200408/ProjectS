package com.project.ProjectS.repository;

import com.project.ProjectS.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository
        extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExam_ExamId(Long examId);

    boolean existsByExam_ExamIdAndQuestion_QuestionId(
            Long examId,
            Long questionId
    );

    void deleteByExam_ExamIdAndQuestion_QuestionId(
            Long examId,
            Long questionId
    );
}