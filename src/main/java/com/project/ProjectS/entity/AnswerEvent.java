package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "answer_events")
@Getter
@Setter
public class AnswerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_event_id")
    private Long answerEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private TableAttribute attribute;
    @Column(name = "answer_position")
    private Integer answerPosition;
    @Column(name = "arithmetic", length = 50)
    private String arithmetic;
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "is_correct")
    private Boolean isCorrect;
    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "marks", precision = 10, scale = 2)
    private BigDecimal marks;

    @Column(name = "hint", columnDefinition = "TEXT")
    private String hint;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;


    @Column(name = "active_row")
    private Boolean activeRow = true;


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (activeRow == null) {
            activeRow = true;
        }
    }


    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }
}