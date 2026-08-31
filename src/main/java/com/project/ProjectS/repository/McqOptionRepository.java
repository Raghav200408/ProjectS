package com.project.ProjectS.repository;

import com.project.ProjectS.entity.McqOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McqOptionRepository
        extends JpaRepository<McqOption, Long> {

    List<McqOption>
    findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
            Long questionId
    );
}