package com.project.ProjectS.controller;

import com.project.ProjectS.model.PracticePerformanceResponseDTO;
import com.project.ProjectS.repository.PracticePerformanceRepository.Filters;
import com.project.ProjectS.repository.PracticePerformanceRepository.Level;
import com.project.ProjectS.service.PracticePerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/*
 * Practice performance, from practice_results (current state) plus the
 * question / unit definitions. The user comes from the login, not the URL.
 * courseId / subjectId / chapterId narrow the hierarchy (each level applies the
 * ones it has a column for); collegeId / branchId / studentId are optional
 * filters that only narrow what the caller's role already allows.
 */
@RestController
@RequestMapping("/api/performance/practice")
public class PracticePerformanceController {

    private final PracticePerformanceService performanceService;

    @Autowired
    public PracticePerformanceController(
            PracticePerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/course")
    public ResponseEntity<PracticePerformanceResponseDTO> courses(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(performanceService.getPerformance(
                Level.COURSE, new Filters(courseId, subjectId, chapterId),
                collegeId, branchId, studentId, authentication));
    }

    @GetMapping("/subject")
    public ResponseEntity<PracticePerformanceResponseDTO> subjects(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(performanceService.getPerformance(
                Level.SUBJECT, new Filters(courseId, subjectId, chapterId),
                collegeId, branchId, studentId, authentication));
    }

    @GetMapping("/chapter")
    public ResponseEntity<PracticePerformanceResponseDTO> chapters(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(performanceService.getPerformance(
                Level.CHAPTER, new Filters(courseId, subjectId, chapterId),
                collegeId, branchId, studentId, authentication));
    }

    @GetMapping("/topic")
    public ResponseEntity<PracticePerformanceResponseDTO> topics(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(performanceService.getPerformance(
                Level.TOPIC, new Filters(courseId, subjectId, chapterId),
                collegeId, branchId, studentId, authentication));
    }

    // No global exception handler exists, so report the reason for a
    // rejected request instead of a bare 500.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(
            ResponseStatusException ex) {

        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", String.valueOf(ex.getReason())));
    }
}
