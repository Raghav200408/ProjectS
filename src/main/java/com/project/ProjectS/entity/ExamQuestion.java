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
                        columnNames = {
                                "exam_id",
                                "question_id"
                        }
                )
        }
)
@Getter
@Setter
public class ExamQuestion {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "exam_question_seq"
    )
    @SequenceGenerator(
            name = "exam_question_seq",
            sequenceName = "exam_questions_exam_question_id_seq",
            allocationSize = 1
    )
    @Column(name = "exam_question_id")
    private Long examQuestionId;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}