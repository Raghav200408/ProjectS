package com.project.ProjectS.repository;
import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByBranchNameAndCollege(
            String branchName,
            College college
    );
    List<Branch> findByBranchName(String branchName);


}