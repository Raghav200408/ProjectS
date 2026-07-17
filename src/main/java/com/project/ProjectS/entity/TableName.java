package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_names")
@Getter
@Setter
public class TableName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_name_id")
    private Long tableNameId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "active_row")
    private Boolean activeRow;

    @Column(name = "row_status")
    private Integer rowStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

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