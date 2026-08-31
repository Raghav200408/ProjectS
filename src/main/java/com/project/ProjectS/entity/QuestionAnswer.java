package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_answers")
@Getter
@Setter
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;


    // User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // Question
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;


    // Table Name
    @ManyToOne
    @JoinColumn(name = "table_name_id")
    private TableName tableName;


    // Table Header
    @ManyToOne
    @JoinColumn(name = "header_id")
    private TableHeader header;


    // Table Attribute
    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private TableAttribute attribute;

    // Table Pair Attribute
    @ManyToOne
    @JoinColumn(name = "pair_attribute_id")
    private TableAttribute pairAttribute;

    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;


    // Arithmetic
    @Column(name = "arithmetic")
    private String arithmetic;


    // Amount
    @Column(name = "amount")
    private BigDecimal amount;

    // Total Answers
    @Column(name = "total_answers")
    private Long totalAnswers;

    // Condition Id
    @Column(name = "condition_id")
    private Long conditionId;


    // Active Row
    @Column(name = "active_row")
    private Boolean activeRow = true;


    // Created At
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    // Row Status
    @Column(name = "row_status")
    private Integer rowStatus = 1;


    // Updated At
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

        if (rowStatus == null) {
            rowStatus = 1;
        }
    }


    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }
}