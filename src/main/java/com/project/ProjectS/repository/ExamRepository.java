package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCollege_CollegeId(Long collegeId);

    List<Exam> findByBranch_BranchId(Long branchId);

    List<Exam> findBySection_SectionId(Long sectionId);

    /**
     * Exams for a student's section, excluding any the student has
     * already submitted (i.e. that have a row in exam_result for them).
     */
    @Query("""
            select e from Exam e
            where e.section.sectionId = :sectionId
              and e.examId not in (
                  select r.exam.examId from ExamResult r
                  where r.user.userId = :userId
              )
            """)
    List<Exam> findUnattemptedForStudent(@Param("sectionId") Long sectionId,
                                         @Param("userId") Long userId);
}
