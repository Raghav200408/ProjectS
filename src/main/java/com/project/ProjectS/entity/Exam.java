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
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "exam_seq"
    )
    @SequenceGenerator(
            name = "exam_seq",
            sequenceName = "exams_exam_id_seq",
            allocationSize = 1
    )
    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "exam_name", nullable = false)
    private String examName;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "active_row")
    private Boolean activeRow = true;

    @Column(name = "row_status")
    private Integer rowStatus = 1;

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

        if (rowStatus == null) {
            rowStatus = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}