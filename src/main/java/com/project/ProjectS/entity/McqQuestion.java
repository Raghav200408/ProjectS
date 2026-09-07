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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_type_id", nullable = false)
    private QuestionType questionType;

    @Column(name = "marks", nullable = false)
    private Double marks = 1.0;

    @Column(name = "active_row", nullable = false)
    private Boolean activeRow = true;
}
