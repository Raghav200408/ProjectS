package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mcq_questions")
public class McqQuestion {

    @Id
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_type", nullable = false)
    private String questionType = "SINGLE_CHOICE";

    @Column(name = "marks", nullable = false)
    private Double marks = 1.0;

    @Column(name = "active_row", nullable = false)
    private Boolean activeRow = true;
}