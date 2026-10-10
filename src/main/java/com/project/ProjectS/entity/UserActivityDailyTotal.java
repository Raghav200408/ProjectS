package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(
        name = "user_activity_daily_totals",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_activity_daily_totals_user_date",
                columnNames = {"user_id", "activity_date"}
        )
)
public class UserActivityDailyTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "active_seconds", nullable = false)
    private Long activeSeconds = 0L;
}
