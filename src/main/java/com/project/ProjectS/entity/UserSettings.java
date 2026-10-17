package com.project.ProjectS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
public class UserSettings {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "notification_popups", nullable = false)
    private boolean notificationPopups = true;
    @Column(name = "activity_reminders", nullable = false)
    private boolean activityReminders = true;
    @Column(name = "product_announcements", nullable = false)
    private boolean productAnnouncements = false;
}
