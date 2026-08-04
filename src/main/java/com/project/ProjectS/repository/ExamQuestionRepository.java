package com.project.ProjectS.repository;

import com.project.ProjectS.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ExamQuestionRepository
        extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExam_ExamId(Long examId);

    boolean existsByExam_ExamIdAndQuestion_QuestionId(
            Long examId,
            Long questionId
    );
    @Modifying
    @Transactional
    @Query("DELETE FROM ExamQuestion eq WHERE eq.exam.examId = :examId")
    void deleteExamQuestions(@Param("examId") Long examId);

    @Transactional
    @Modifying
    void deleteByExam_ExamId(Long examId);
}