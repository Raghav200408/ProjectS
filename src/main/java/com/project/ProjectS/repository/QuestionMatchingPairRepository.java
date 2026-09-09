package com.project.ProjectS.repository;

import com.project.ProjectS.entity.QuestionMatchingPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionMatchingPairRepository
        extends JpaRepository<QuestionMatchingPair, Long> {

    List<QuestionMatchingPair> findByQuestion_QuestionIdOrderByDisplayOrderAsc(
            Long questionId
    );

    void deleteByQuestion_QuestionId(Long questionId);
}