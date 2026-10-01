package com.project.ProjectS.service;

import com.project.ProjectS.entity.Notification;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.NotificationResponseDTO;
import com.project.ProjectS.repository.NotificationRepository;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {
    private static final Set<String> ALLOWED_EVENT_TYPES = Set.of(
            "EXAM_ASSIGNED", "EXAM_RESCHEDULED", "EXAM_CANCELLED", "EXAM_REMINDER",
            "RESULT_PUBLISHED", "SUBSCRIPTION_EXPIRING", "SUBSCRIPTION_EXPIRED",
            "ATTENDANCE_WARNING", "ATTENDANCE_SUBMISSION_MISSING", "CONTENT_ASSIGNED",
            "UPLOAD_COMPLETED", "UPLOAD_FAILED", "CERTIFICATE_GENERATED", "SYSTEM_ANNOUNCEMENT"
    );

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> mine(User user, int limit) {
        List<Notification> notifications = visible(user, limit);
        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return notificationRepository.countUnread(user.getUserId(), LocalDateTime.now());
    }

    @Transactional
    public void markRead(User user, Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndRecipientUserId(notificationId, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllRead(User user) {
        notificationRepository.markAllRead(user.getUserId(), LocalDateTime.now());
    }

    /** Internal API for approved backend events; routine CRUD must keep using toasts. */
    @Transactional
    public Notification publish(Notification notification) {
        if (!ALLOWED_EVENT_TYPES.contains(notification.getEventType())) {
            throw new IllegalArgumentException("Unsupported notification event: " + notification.getEventType());
        }
        List<User> recipients = userRepository.findNotificationRecipients(
                notification.getRecipientType(), notification.getRecipientUserId(), notification.getRecipientRole(),
                notification.getCollegeId(), notification.getBranchId(), notification.getCourseId(),
                notification.getSectionId());
        if (recipients.isEmpty()) return notification;

        List<Notification> copies = recipients.stream()
                .map(user -> copyForRecipient(notification, user.getUserId()))
                .toList();
        return notificationRepository.saveAll(copies).get(0);
    }

    private List<Notification> visible(User user, int limit) {
        return notificationRepository.findForUser(user.getUserId(), LocalDateTime.now(),
                PageRequest.of(0, Math.max(1, Math.min(limit, 100)))
        );
    }

    private Notification copyForRecipient(Notification source, Long recipientUserId) {
        Notification copy = new Notification();
        copy.setEventType(source.getEventType());
        copy.setTitle(source.getTitle());
        copy.setMessage(source.getMessage());
        copy.setSeverity(source.getSeverity());
        copy.setRecipientType("USER");
        copy.setRecipientUserId(recipientUserId);
        copy.setRecipientRole(source.getRecipientRole());
        copy.setCollegeId(source.getCollegeId());
        copy.setBranchId(source.getBranchId());
        copy.setCourseId(source.getCourseId());
        copy.setSectionId(source.getSectionId());
        copy.setActionUrl(source.getActionUrl());
        copy.setExpiresAt(source.getExpiresAt());
        return copy;
    }

    private NotificationResponseDTO toResponse(Notification notification) {
        return new NotificationResponseDTO(
                notification.getNotificationId(), notification.getEventType(), notification.getTitle(),
                notification.getMessage(), notification.getSeverity(), notification.getActionUrl(),
                notification.getReadAt() != null, notification.getCreatedAt());
    }
}
