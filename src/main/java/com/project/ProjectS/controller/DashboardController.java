package com.project.ProjectS.controller;

import com.project.ProjectS.model.DashboardResponseDTO;
import com.project.ProjectS.service.DashboardService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    public DashboardController(DashboardService service) {
        this.service = service;
    }


    private static final Logger log =
            LogManager.getLogger(DashboardController.class);
    private final DashboardService service;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard(Authentication authentication) {

        log.info("Received request to fetch dashboard details.");

        DashboardResponseDTO response = service.getDashboard(authentication);

        log.info("Dashboard details fetched successfully.");

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode())
                .body(Map.of("message", error.getReason() == null ? "Dashboard request rejected" : error.getReason()));
    }
}
