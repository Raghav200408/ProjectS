package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;
    @Column(nullable = false)
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private boolean active = true;
    private Integer practiceQuestionsUsed = 0;
    private Integer mockTestsUsed = 0;
    private Integer examAttemptsUsed = 0;

    @PrePersist
    void prePersist() { if (startsAt == null) startsAt = LocalDateTime.now(); }
}
