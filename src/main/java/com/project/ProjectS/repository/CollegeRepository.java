package com.project.ProjectS.repository;

import com.project.ProjectS.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    boolean existsByInstituteName(String instituteName);

    Optional<College> findByInstituteName(String instituteName);

}