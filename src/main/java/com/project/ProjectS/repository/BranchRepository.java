package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByBranchName(String branchName);
    List<Branch> findByBranchName(String branchName);
    Optional<Branch> findByBranchNameAndCollege(
            String branchName,
            College college
    );

}