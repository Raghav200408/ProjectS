package com.project.ProjectS.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AuditLogger {

    private static final Logger auditLog = LogManager.getLogger("AUDIT");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String action, String userEmail, String resourceType, Long resourceId, String outcome, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        auditLog.info("timestamp={} action={} userEmail={} resourceType={} resourceId={} outcome={} details={}",
                timestamp, action, userEmail, resourceType, resourceId, outcome, details);
    }

    public void log(String action, String userEmail, String resourceType, Long resourceId, String outcome) {
        log(action, userEmail, resourceType, resourceId, outcome, "");
    }
}
