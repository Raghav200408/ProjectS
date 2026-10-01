package com.project.ProjectS.repository;

import com.project.ProjectS.entity.UserActivitySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserActivitySessionRepository extends JpaRepository<UserActivitySession, Long> {
    Optional<UserActivitySession> findBySessionKeyAndUser_UserId(String sessionKey, Long userId);

    List<UserActivitySession> findByUser_UserIdAndStatusIn(Long userId, Collection<String> statuses);

    List<UserActivitySession> findByStatusAndLastHeartbeatAtBefore(String status, LocalDateTime cutoff);

    List<UserActivitySession> findByStatusInAndLastInteractionAtBefore(
            Collection<String> statuses,
            LocalDateTime cutoff
    );
}