package com.project.ProjectS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.ProjectS.entity.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

}