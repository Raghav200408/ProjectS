package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    boolean existsByName(String name);

    Optional<Chapter> findByName(String name);


}