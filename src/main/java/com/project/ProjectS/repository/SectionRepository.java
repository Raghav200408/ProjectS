package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    boolean existsBySectionName(String sectionName);

    Optional<Section> findBySectionName(String sectionName);

}