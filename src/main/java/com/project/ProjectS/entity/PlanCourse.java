package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plan_courses", uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "course_id"}))
@Getter
@Setter
public class PlanCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planCourseId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;
}
