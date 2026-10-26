package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "courseassignment", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "branch_id"}))
@Getter @Setter
public class CourseAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "courseassignment_id")
    private Long assignmentId;
    @ManyToOne(optional = false) @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @ManyToOne(optional = false) @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @Column(name = "active_row", nullable = false)
    private Boolean activeRow = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist void create() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void update() { updatedAt = LocalDateTime.now(); }
}
