package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByBranchName(String branchName);

    Optional<Branch> findByBranchName(String branchName);

}