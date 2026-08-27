package com.project.ProjectS.repository;

import com.project.ProjectS.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionTypeRepository extends JpaRepository<QuestionType, Long> {

    boolean existsByQuestionType(String questionType);

    Optional<QuestionType> findByQuestionType(String questionType);
}
