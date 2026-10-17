package com.project.ProjectS.controller;

import com.project.ProjectS.model.UserSettingsDTO;
import com.project.ProjectS.service.UserSettingsService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/mine")
public class UserSettingsController {
    private final UserSettingsService service;
    public UserSettingsController(UserSettingsService service) { this.service = service; }
    @GetMapping
    public UserSettingsDTO get(Principal principal) { return service.get(principal); }
    @PutMapping
    public UserSettingsDTO save(Principal principal, @Valid @RequestBody UserSettingsDTO request) {
        return service.save(principal, request);
    }
}
