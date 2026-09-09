package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mock_exam")
@Getter
@Setter
public class MockExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mock_exam_id")
    private Long mockExamId;

    @Column(name = "mock_exam_name", nullable = false)
    private String mockExamName;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToMany
    @JoinTable(
            name = "mock_exam_chapters",
            joinColumns = @JoinColumn(name = "mock_exam_id"),
            inverseJoinColumns = @JoinColumn(name = "chapter_id")
    )
    private List<Chapter> chapters;

    @Column(name = "pass_percentage", nullable = false)
    private Integer passPercentage;

    @Column(name = "active_row")
    private Boolean activeRow = true;

    @Column(name = "row_status")
    private Integer rowStatus = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
