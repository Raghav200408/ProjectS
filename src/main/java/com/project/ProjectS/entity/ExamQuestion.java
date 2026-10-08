package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_question",
                        columnNames = {"exam_id", "question_id"}
                )
        }
)
@Getter
@Setter
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_question_id")
    private Long examQuestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "exam_id",
            nullable = false
    )
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "question_id",
            nullable = false
    )
    private Question question;

    // How many marks this question is worth on this exam's paper. Set once,
    // when the question is added (ExamService.addQuestionsToExam), from
    // ExamScoringService.computeQuestionMaxMarks - the same number
    // score() works out live, kept here so the Performance dashboard's
    // chapter breakdown can read it without recomputing it.
    @Column(name = "marks", nullable = false)
    private Double marks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}