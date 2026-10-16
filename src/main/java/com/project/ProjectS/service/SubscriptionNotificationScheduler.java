package com.project.ProjectS.service;

import com.project.ProjectS.repository.UserSubscriptionRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SubscriptionNotificationScheduler {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionNotificationScheduler.class);
    private final UserSubscriptionRepository subscriptions;
    private final NotificationEventDelivery delivery;

    public SubscriptionNotificationScheduler(UserSubscriptionRepository subscriptions, NotificationEventDelivery delivery) {
        this.subscriptions = subscriptions;
        this.delivery = delivery;
    }

    @Scheduled(fixedDelayString = "${notifications.subscription-check-ms:3600000}", initialDelay = 60000)
    public void check() {
        for (Long id : subscriptions.findNotificationCandidates(LocalDateTime.now().plusDays(3))) {
            try { delivery.subscriptionChanged(id, false); }
            catch (RuntimeException error) { log.error("Could not deliver subscription lifecycle alert for {}", id, error); }
        }
    }
}
