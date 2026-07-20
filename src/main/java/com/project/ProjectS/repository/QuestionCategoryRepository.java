package com.project.ProjectS.repository;

import com.project.ProjectS.entity.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, Long> {

    boolean existsByName(String name);

    Optional<QuestionCategory> findByName(String name);
}