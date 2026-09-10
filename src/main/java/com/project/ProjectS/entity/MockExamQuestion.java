package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "mock_exam_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mock_exam_question",
                        columnNames = {"mock_exam_id", "question_id"}
                )
        }
)
@Getter
@Setter
public class MockExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mock_exam_question_id")
    private Long mockExamQuestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "mock_exam_id",
            nullable = false
    )
    private MockExam mockExam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "question_id",
            nullable = false
    )
    private Question question;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}