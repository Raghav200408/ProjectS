package com.project.ProjectS.controller;

import com.project.ProjectS.model.*;
import com.project.ProjectS.service.McqQuestionService;
import com.project.ProjectS.service.MockTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mock-tests")
public class MockTestController {
    private final MockTestService mockTestService;
    private final McqQuestionService mcqQuestionService;

    public MockTestController(MockTestService mockTestService, McqQuestionService mcqQuestionService) {
        this.mockTestService = mockTestService;
        this.mcqQuestionService = mcqQuestionService;
    }

    @PostMapping("/start")
    public MockTestResponseDTO start(@RequestBody MockTestStartRequestDTO request,
                                     Authentication authentication) {
        return mockTestService.start(request, authentication.getName());
    }

    @PostMapping("/submit")
    public ResponseEntity<McqSubmissionResponseDTO> submit(
            @RequestBody McqSubmissionRequestDTO request,
            Authentication authentication) {
        request.setMockTest(true);
        return ResponseEntity.ok(mcqQuestionService.submitMcqAnswersInternal(
                request));
    }
}
