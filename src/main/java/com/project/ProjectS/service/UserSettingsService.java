package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.entity.UserSettings;
import com.project.ProjectS.model.UserSettingsDTO;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.repository.UserSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;

@Service
public class UserSettingsService {
    private final UserSettingsRepository settings;
    private final UserRepository users;
    public UserSettingsService(UserSettingsRepository settings, UserRepository users) {
        this.settings = settings;
        this.users = users;
    }

    private User currentUser(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return users.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @Transactional(readOnly = true)
    public UserSettingsDTO get(Principal principal) {
        return settings.findById(currentUser(principal).getUserId()).map(this::response)
                .orElseGet(UserSettingsDTO::defaults);
    }

    @Transactional
    public UserSettingsDTO save(Principal principal, UserSettingsDTO request) {
        // Lock the owner row to serialize first-time inserts from multiple tabs.
        Long userId = currentUser(principal).getUserId();
        users.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        UserSettings saved = settings.findById(userId).orElseGet(UserSettings::new);
        saved.setUserId(userId);
        saved.setNotificationPopups(request.notificationPopups());
        saved.setActivityReminders(request.activityReminders());
        saved.setProductAnnouncements(request.productAnnouncements());
        return response(settings.save(saved));
    }

    private UserSettingsDTO response(UserSettings saved) {
        return new UserSettingsDTO(saved.isNotificationPopups(), saved.isActivityReminders(),
                saved.isProductAnnouncements());
    }
}
