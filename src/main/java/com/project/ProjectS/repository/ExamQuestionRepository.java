package com.project.ProjectS.repository;

import com.project.ProjectS.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExamId(Long examId);

    List<ExamQuestion> findByHeaderId(Long headerId);

    List<ExamQuestion> findByAttributeId(Long attributeId);

}