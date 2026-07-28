package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "question_attributes")
public class QuestionAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_attribute_id")
    private Long questionAttributeId;


    // Question
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;


    // Table Header
    @ManyToOne
    @JoinColumn(name = "header_id")
    private TableHeader header;


    // Table Attribute
    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private TableAttribute attribute;


    // Transaction Date
    @Column(name = "transaction_date")
    private LocalDate transactionDate;


    // Amount 1
    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;


    // Amount 2
    @Column(name = "amount2", precision = 18, scale = 2)
    private BigDecimal amount2;


    // Note
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


    // Active Row
    @Column(name = "active_row")
    private Boolean activeRow = true;


    // Created At
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    // Updated At
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {

        if (activeRow == null) {
            activeRow = true;
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}