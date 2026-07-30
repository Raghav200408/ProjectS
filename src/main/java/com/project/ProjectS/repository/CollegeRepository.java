package com.project.ProjectS.repository;

import com.project.ProjectS.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    boolean existsByInstituteName(String instituteName);

    List<College> findByInstituteName(String instituteName);

}