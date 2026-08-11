package com.project.ProjectS.entity;
import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "answer_id")
    private QuestionAnswer answer;

    @Column(name = "description")
    private String description;

    @Column(name = "valid")
    private Boolean valid;

    @Column(name = "action")
    private String action;

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "answer_by")
    private String answerBy;

    @Column(name = "hint")
    private String hint;

    @Column(name = "active_row")
    private Boolean activeRow = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "table_name_id")
    private TableName tableName;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private TableHeader header;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private TableAttribute attribute;

    @Column(name = "arithmetic")
    private String arithmetic;

    @Column(name = "amount")
    private BigDecimal amount;

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