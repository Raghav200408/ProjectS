package com.project.ProjectS.repository;

import com.project.ProjectS.entity.McqQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McqQuestionRepository
        extends JpaRepository<McqQuestion, Long> {

    List<McqQuestion> findByActiveRowTrue();
}