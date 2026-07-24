package com.project.ProjectS.controller;

import com.project.ProjectS.model.DashboardResponseDTO;
import com.project.ProjectS.service.DashboardService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger =
            LogManager.getLogger(DashboardController.class);

    @Autowired
    private DashboardService service;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard() {

        logger.info("Received request to fetch dashboard details.");

        DashboardResponseDTO response = service.getDashboard();

        logger.info("Dashboard details fetched successfully.");

        return ResponseEntity.ok(response);
    }
}