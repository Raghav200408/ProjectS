package com.project.ProjectS.repository;

import com.project.ProjectS.entity.PlanQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanQuestionRepository extends JpaRepository<PlanQuestion, Long> {
    List<PlanQuestion> findByPlanCourse_PlanCourseId(Long planCourseId);
    void deleteByPlanCourse_PlanCourseId(Long planCourseId);
    boolean existsByPlanCourse_PlanCourseIdAndQuestion_QuestionId(Long planCourseId, Long questionId);
}
