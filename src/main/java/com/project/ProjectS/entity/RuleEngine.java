package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rule_engines")
@Getter
@Setter
public class RuleEngine {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "rule_engine_seq"
    )
    @SequenceGenerator(
            name = "rule_engine_seq",
            sequenceName = "rule_engines_rule_engine_id_seq",
            allocationSize = 1
    )
    @Column(name = "rule_engine_id")
    private Long ruleEngineId;

    @ManyToOne
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne
    @JoinColumn(name = "pair_attribute_id")
    private TableAttribute pairAttribute;

    @ManyToOne
    @JoinColumn(name = "field_name", referencedColumnName = "name")
    private TableAttribute fieldName;

    @ManyToOne
    @JoinColumn(name = "field_type", referencedColumnName = "name")
    private TableHeader fieldType;

    @Column(name = "relationship_name", length = 100)
    private String relationshipName;

    @Column(name = "pair_order")
    private Integer pairOrder;


//condition 1
    @Column(name = "arithmetic1", length = 20)
    private String arithmetic1;

    @ManyToOne
    @JoinColumn(name = "table1_id")
    private TableName table1;

    @ManyToOne
    @JoinColumn(name = "header1_id")
    private TableHeader header1;

    @Column(name = "amount_position1", length = 20)
    private String amountPosition1;

    @Column(name = "information1", columnDefinition = "TEXT")
    private String information1;
//condition2
    @Column(name = "arithmetic2", length = 20)
    private String arithmetic2;

    @ManyToOne
    @JoinColumn(name = "table2_id")
    private TableName table2;

    @ManyToOne
    @JoinColumn(name = "header2_id")
    private TableHeader header2;

    @Column(name = "amount_position2", length = 20)
    private String amountPosition2;

    @Column(name = "information2", columnDefinition = "TEXT")
    private String information2;
    // Condition 3
    @Column(name = "arithmetic3", length = 20)
    private String arithmetic3;

    @ManyToOne
    @JoinColumn(name = "table3_id")
    private TableName table3;

    @ManyToOne
    @JoinColumn(name = "header3_id")
    private TableHeader header3;

    @Column(name = "amount_position3", length = 20)
    private String amountPosition3;

    @Column(name = "information3", columnDefinition = "TEXT")
    private String information3;

    //Condition 4

    @Column(name = "arithmetic4", length = 20)
    private String arithmetic4;

    @ManyToOne
    @JoinColumn(name = "table4_id")
    private TableName table4;

    @ManyToOne
    @JoinColumn(name = "header4_id")
    private TableHeader header4;

    @Column(name = "amount_position4", length = 20)
    private String amountPosition4;

    @Column(name = "information4", columnDefinition = "TEXT")
    private String information4;

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
