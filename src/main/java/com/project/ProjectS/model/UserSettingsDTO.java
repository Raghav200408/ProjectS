package com.project.ProjectS.model;

import jakarta.validation.constraints.NotNull;

public record UserSettingsDTO(@NotNull Boolean notificationPopups,
                              @NotNull Boolean activityReminders,
                              @NotNull Boolean productAnnouncements) {
    public static UserSettingsDTO defaults() { return new UserSettingsDTO(true, true, false); }
}
