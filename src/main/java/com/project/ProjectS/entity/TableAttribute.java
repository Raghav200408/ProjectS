package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "table_attributes")
public class TableAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    private Long attributeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "row_status")
    private Boolean rowStatus = true;

    @Column(name = "active_row")
    private Boolean activeRow = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "header_id", nullable = false)
    private TableHeader tableHeader;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (activeRow == null) {
            activeRow = true;
        }

        if (rowStatus == null) {
            rowStatus = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}