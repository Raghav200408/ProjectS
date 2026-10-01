package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
            select n from Notification n
            where n.recipientUserId = :userId
              and (n.expiresAt is null or n.expiresAt > :now)
            order by n.createdAt desc
            """)
    List<Notification> findForUser(@Param("userId") Long userId,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

    @Query("""
            select count(n) from Notification n
            where n.recipientUserId = :userId and n.readAt is null
              and (n.expiresAt is null or n.expiresAt > :now)
            """)
    long countUnread(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    java.util.Optional<Notification> findByNotificationIdAndRecipientUserId(Long notificationId, Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("update Notification n set n.readAt = :readAt where n.recipientUserId = :userId and n.readAt is null")
    int markAllRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}
