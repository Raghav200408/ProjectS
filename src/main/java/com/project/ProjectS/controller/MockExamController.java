package com.project.ProjectS.controller;

import com.project.ProjectS.model.*;
import com.project.ProjectS.service.MockExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mock-exams")
public class MockExamController {

    private final MockExamService mockExamService;

    public MockExamController(MockExamService mockExamService) {
        this.mockExamService = mockExamService;
    }


    @PostMapping
    public ResponseEntity<MockExamResponseDTO> createMockExam(
            @Valid @RequestBody MockExamRequestDTO request) {

        MockExamResponseDTO response =
                mockExamService.createMockExam(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping
    public ResponseEntity<List<MockExamResponseDTO>> getAllMockExams() {

        return ResponseEntity.ok(
                mockExamService.getAllMockExams()
        );
    }


    @GetMapping("/{mockExamId}")
    public ResponseEntity<MockExamResponseDTO> getMockExamById(
            @PathVariable Long mockExamId) {

        return ResponseEntity.ok(
                mockExamService.getMockExamById(mockExamId)
        );
    }


    @PutMapping("/{mockExamId}")
    public ResponseEntity<MockExamResponseDTO> updateMockExam(
            @PathVariable Long mockExamId,
            @Valid @RequestBody MockExamRequestDTO request) {

        return ResponseEntity.ok(
                mockExamService.updateMockExam(
                        mockExamId,
                        request
                )
        );
    }


    @DeleteMapping("/{mockExamId}")
    public ResponseEntity<String> deleteMockExam(
            @PathVariable Long mockExamId) {

        mockExamService.deleteMockExam(mockExamId);

        return ResponseEntity.ok(
                "Mock exam deleted successfully"
        );
    }


    @PostMapping("/{mockExamId}/questions")
    public ResponseEntity<String> addQuestionsToMockExam(
            @PathVariable Long mockExamId,
            @Valid @RequestBody AddMockExamQuestionsRequestDTO request) {

        mockExamService.addQuestionsToMockExam(
                mockExamId,
                request
        );

        return ResponseEntity.ok(
                "Questions added to mock exam successfully"
        );
    }


    @GetMapping("/{mockExamId}/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getMockExamQuestions(
            @PathVariable Long mockExamId,
            Authentication authentication) {

        return ResponseEntity.ok(
                mockExamService.getMockExamQuestions(mockExamId, authentication)
        );
    }


    @DeleteMapping("/{mockExamId}/questions/{questionId}")
    public ResponseEntity<String> removeQuestionFromMockExam(
            @PathVariable Long mockExamId,
            @PathVariable Long questionId) {

        mockExamService.removeQuestionFromMockExam(
                mockExamId,
                questionId
        );

        return ResponseEntity.ok(
                "Question removed from mock exam successfully"
        );
    }


    @GetMapping("/{mockExamId}/available-questions")
    public ResponseEntity<List<QuestionResponseDTO>> getAvailableQuestions(
            @PathVariable Long mockExamId) {

        return ResponseEntity.ok(
                mockExamService.getAvailableMockExamQuestions(mockExamId)
        );
    }


    @PostMapping("/{mockExamId}/submit")
    public ResponseEntity<MockExamSubmitResponseDTO> submitMockExam(
            @PathVariable Long mockExamId,
            @RequestBody MockExamSubmitRequestDTO request,
            Authentication authentication) {

        MockExamSubmitResponseDTO response =
                mockExamService.submitMockExam(mockExamId, request, authentication);

        return ResponseEntity.ok(response);
    }
}
