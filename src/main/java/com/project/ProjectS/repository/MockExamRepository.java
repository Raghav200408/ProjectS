package com.project.ProjectS.repository;

import com.project.ProjectS.entity.MockExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockExamRepository extends JpaRepository<MockExam, Long> {
}
