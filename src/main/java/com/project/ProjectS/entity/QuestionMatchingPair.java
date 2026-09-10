package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "question_matching_pairs")
public class QuestionMatchingPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pair_id")
    private Long pairId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "column_a", nullable = false, columnDefinition = "TEXT")
    private String columnA;

    @Column(name = "column_b", nullable = false, columnDefinition = "TEXT")
    private String columnB;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}