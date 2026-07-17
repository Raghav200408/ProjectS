package com.project.ProjectS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.ProjectS.entity.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

}