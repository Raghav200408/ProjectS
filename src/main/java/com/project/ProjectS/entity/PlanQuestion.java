package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plan_questions", uniqueConstraints = @UniqueConstraint(columnNames = {"plan_course_id", "question_id"}))
@Getter
@Setter
public class PlanQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planQuestionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_course_id", nullable = false)
    private PlanCourse planCourse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
