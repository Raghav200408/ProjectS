package com.project.ProjectS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.ProjectS.entity.College;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

}