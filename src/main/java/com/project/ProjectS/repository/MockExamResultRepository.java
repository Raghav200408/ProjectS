package com.project.ProjectS.repository;

import com.project.ProjectS.entity.MockExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MockExamResultRepository
        extends JpaRepository<MockExamResult, Long> {

    Optional<MockExamResult> findByMockExam_MockExamIdAndUser_UserId(
            Long mockExamId,
            Long userId
    );
}
