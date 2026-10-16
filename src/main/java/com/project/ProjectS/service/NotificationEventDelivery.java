package com.project.ProjectS.service;

import com.project.ProjectS.entity.Notification;
import com.project.ProjectS.repository.NotificationRepository;
import com.project.ProjectS.repository.UserSubscriptionRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** A separate transaction keeps notification delivery independent of the completed operation. */
@Service
public class NotificationEventDelivery {
    private final NotificationService notifications;
    private final NotificationRepository repository;
    private final UserSubscriptionRepository subscriptions;

    public NotificationEventDelivery(NotificationService notifications, NotificationRepository repository,
                                     UserSubscriptionRepository subscriptions) {
        this.notifications = notifications;
        this.repository = repository;
        this.subscriptions = subscriptions;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(Long userId, String event, String title, String message, String url) {
        if (userId == null) return;
        Notification notification = new Notification();
        notification.setRecipientType("USER");
        notification.setRecipientUserId(userId);
        notification.setEventType(event);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSeverity(event.endsWith("FAILED") ? "WARNING" : "INFO");
        notification.setActionUrl(url);
        notifications.publish(notification);
    }

    public boolean alreadySent(Long userId, String event, String url) {
        return repository.existsByRecipientUserIdAndEventTypeAndActionUrl(userId, event, url);
    }

    /** Lock the subscription so concurrent scheduler instances cannot send duplicate reminders. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void subscriptionChanged(Long id, boolean deactivated) {
        var subscription = subscriptions.findForNotification(id).orElse(null);
        if (subscription == null) return;
        LocalDateTime now = LocalDateTime.now();
        if (!deactivated && (!subscription.isActive() || subscription.getExpiresAt() == null
                || subscription.getExpiresAt().isAfter(now.plusDays(3)))) return;
        String event = deactivated ? "SUBSCRIPTION_DEACTIVATED"
                : subscription.getExpiresAt().isAfter(now) ? "SUBSCRIPTION_EXPIRING" : "SUBSCRIPTION_EXPIRED";
        Long userId = subscription.getUser().getUserId();
        String url = "/course-subscription?subscriptionId=" + id;
        if (alreadySent(userId, event, url)) return;
        String title = deactivated ? "Subscription deactivated"
                : event.equals("SUBSCRIPTION_EXPIRING") ? "Subscription expiring soon" : "Subscription expired";
        send(userId, event, title, deactivated ? "Your subscription has been deactivated."
                : "Your subscription expires at " + subscription.getExpiresAt() + ". View your subscriptions for details.", url);
    }
}
