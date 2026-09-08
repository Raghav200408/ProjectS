package com.project.ProjectS.repository;

import com.project.ProjectS.entity.PlanCourse;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface PlanCourseRepository extends JpaRepository<PlanCourse, Long> {
    List<PlanCourse> findByCourse_CourseIdAndPlan_ActiveTrue(Long courseId);
    List<PlanCourse> findByPlan_PlanId(Long planId);
    void deleteByPlan_PlanId(Long planId);
}
