package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
@Getter
@Setter
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long examId;


    @Column(name = "Chapter_id", nullable = false)
    private Long chapterId;


    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "question_code", nullable = false, length = 30)
    private String questionCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "active_row")
    private Boolean activeRow;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (activeRow == null) {
            activeRow = true;

        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
        }


