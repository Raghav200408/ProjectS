package com.project.ProjectS.repository;

import com.project.ProjectS.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    Optional<ExamResult> findByExam_ExamIdAndUser_UserId(
            Long examId,
            Long userId
    );

    List<ExamResult> findByUser_UserIdOrderByCreatedAtAsc(
            Long userId
    );


    List<ExamResult> findByExam_College_CollegeId(
            Long collegeId
    );


    List<ExamResult> findByExam_Branch_BranchId(
            Long branchId
    );
}
