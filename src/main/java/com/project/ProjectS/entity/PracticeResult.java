package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The CURRENT practice result of one unit for one student.
 *
 * answer_events keeps every attempt; this keeps one row per (user, unit) that
 * is updated in place on a repeat attempt - see create_practice_results.sql.
 * Rows are written by native upserts in PracticeResultRepository, so this
 * entity is only used to read them back.
 */
@Entity
@Table(name = "practice_results")
@Getter
@Setter
public class PracticeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_result_id")
    private Long practiceResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Null for MCQ, where the question itself is the unit.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_attribute_id")
    private QuestionAttribute questionAttribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // "ATTRIBUTE" or "MCQ".
    @Column(name = "question_type", nullable = false, length = 30)
    private String questionType;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
