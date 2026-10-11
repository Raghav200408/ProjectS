package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_activity_sessions")
public class UserActivitySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false, unique = true, length = 36)
    private String sessionKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_heartbeat_at", nullable = false)
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "last_interaction_at", nullable = false)
    private LocalDateTime lastInteractionAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_active_seconds", nullable = false)
    private Long totalActiveSeconds = 0L;

    @Column(name = "current_chapter_id")
    private Long currentChapterId;

    @Column(name = "current_chapter_name")
    private String currentChapterName;

    @Column(name = "current_topic_id")
    private Long currentTopicId;

    @Column(name = "current_topic_name")
    private String currentTopicName;

    @Column(name = "current_question_id")
    private Long currentQuestionId;

    @Column(name = "current_question_name")
    private String currentQuestionName;
}
