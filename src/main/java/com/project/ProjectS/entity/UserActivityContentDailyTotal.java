package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "user_activity_content_daily_totals")
public class UserActivityContentDailyTotal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "content_level", nullable = false, length = 16)
    private String contentLevel;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "content_name", nullable = false, length = 500)
    private String contentName;

    @Column(name = "active_seconds", nullable = false)
    private Long activeSeconds;
}
