package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
    private String description;
    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "numeric(12,2) default 0")
    private BigDecimal price = BigDecimal.ZERO;
    private boolean freeTrial;
    private boolean active = true;
    private Integer durationDays;
    private Integer practiceQuestionLimit;
    private boolean mockTestEnabled;
    private Integer mockTestLimit;
    private boolean examEnabled;
    private Integer examAttemptLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }
}
