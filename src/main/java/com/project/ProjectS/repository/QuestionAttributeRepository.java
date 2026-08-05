package com.project.ProjectS.repository;

import com.project.ProjectS.entity.QuestionAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface QuestionAttributeRepository
        extends JpaRepository<QuestionAttribute, Long> {

    List<QuestionAttribute> findByQuestion_QuestionId(Long questionId);
    @Transactional
    @Modifying
    void deleteByQuestion_QuestionId(Long questionId);
}