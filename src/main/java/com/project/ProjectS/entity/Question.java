package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;


    // Course
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;


    // Chapter
    @ManyToOne
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;


    // Topic
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    @ManyToOne
    @JoinColumn(name = "question_type_id")
    private QuestionType questionType;


    // Question Text
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;


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