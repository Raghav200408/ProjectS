package com.project.ProjectS.repository;


import com.project.ProjectS.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamRepository  extends JpaRepository<Exam, Long> {

    boolean existsByQuestionCode(String questionCode);
    Optional<Exam> findByQuestionCode(String questionCode);

}
