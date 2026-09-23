package com.project.ProjectS.controller;

import com.project.ProjectS.model.PracticeResultRequestDTO;
import com.project.ProjectS.model.PracticeResultResponseDTO;
import com.project.ProjectS.service.PracticeResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/practice/results")
public class PracticeResultController {

    private final PracticeResultService practiceResultService;

    @Autowired
    public PracticeResultController(
            PracticeResultService practiceResultService) {
        this.practiceResultService = practiceResultService;
    }

    /*
     * Records one attempted unit (attribute) of a practice question: the
     * historical answer_events row plus the current practice_results row.
     */
    @PostMapping
    public ResponseEntity<PracticeResultResponseDTO> recordResult(
            @RequestBody PracticeResultRequestDTO request,
            Authentication authentication) {

        return ResponseEntity.ok(
                practiceResultService.recordAttributeResult(
                        request, authentication));
    }

    // The app has no global exception handler and doesn't expose exception
    // messages, so without these every failure here would be a bare 500 that
    // hides why the request was rejected.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(
            ResponseStatusException ex) {

        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", String.valueOf(ex.getReason())));
    }

    // e.g. "Answer already autofilled for answer position ..." from the
    // existing answer-event rules.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(
            IllegalStateException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", String.valueOf(ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadInput(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest()
                .body(Map.of("message", String.valueOf(ex.getMessage())));
    }
}
