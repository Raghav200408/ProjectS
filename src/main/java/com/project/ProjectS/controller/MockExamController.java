package com.project.ProjectS.controller;

import com.project.ProjectS.model.*;
import com.project.ProjectS.service.MockExamService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mock-exams")
public class MockExamController {
    private static final Logger log = LogManager.getLogger(MockExamController.class);

    private final MockExamService mockExamService;

    public MockExamController(MockExamService mockExamService) {
        this.mockExamService = mockExamService;
    }

    @PostMapping
    public ResponseEntity<MockExamResponseDTO> createMockExam(
            @Valid @RequestBody MockExamRequestDTO request) {
        log.info("Creating mock exam: mockExamName={} courseId={}", request.getMockExamName(), request.getCourseId());
        MockExamResponseDTO response = mockExamService.createMockExam(request);
        log.info("Mock exam created successfully: mockExamId={}", response.getMockExamId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MockExamResponseDTO>> getAllMockExams(Authentication authentication) {
        log.info("Fetching all mock exams for user={}", authentication == null ? "anonymous" : authentication.getName());
        List<MockExamResponseDTO> response = mockExamService.getAllMockExams(authentication);
        log.info("Fetched {} mock exams", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{mockExamId}")
    public ResponseEntity<MockExamResponseDTO> getMockExamById(@PathVariable Long mockExamId) {
        log.info("Fetching mock exam: mockExamId={}", mockExamId);
        MockExamResponseDTO response = mockExamService.getMockExamById(mockExamId);
        log.info("Mock exam fetched successfully: mockExamId={}", mockExamId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{mockExamId}")
    public ResponseEntity<MockExamResponseDTO> updateMockExam(
            @PathVariable Long mockExamId,
            @Valid @RequestBody MockExamRequestDTO request) {
        log.info("Updating mock exam: mockExamId={} mockExamName={}", mockExamId, request.getMockExamName());
        MockExamResponseDTO response = mockExamService.updateMockExam(mockExamId, request);
        log.info("Mock exam updated successfully: mockExamId={}", mockExamId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{mockExamId}")
    public ResponseEntity<String> deleteMockExam(@PathVariable Long mockExamId) {
        log.info("Deleting mock exam: mockExamId={}", mockExamId);
        mockExamService.deleteMockExam(mockExamId);
        log.info("Mock exam deleted successfully: mockExamId={}", mockExamId);
        return ResponseEntity.ok("Mock exam deleted successfully");
    }

    @PostMapping("/{mockExamId}/questions")
    public ResponseEntity<String> addQuestionsToMockExam(
            @PathVariable Long mockExamId,
            @Valid @RequestBody AddMockExamQuestionsRequestDTO request) {
        log.info("Adding questions to mock exam: mockExamId={} questionCount={}", mockExamId, request.getQuestionIds() == null ? 0 : request.getQuestionIds().size());
        mockExamService.addQuestionsToMockExam(mockExamId, request);
        log.info("Questions added to mock exam successfully: mockExamId={}", mockExamId);
        return ResponseEntity.ok("Questions added to mock exam successfully");
    }

    @GetMapping("/{mockExamId}/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getMockExamQuestions(
            @PathVariable Long mockExamId,
            Authentication authentication) {
        log.info("Fetching mock exam questions: mockExamId={} user={}", mockExamId, authentication == null ? "anonymous" : authentication.getName());
        List<QuestionResponseDTO> response = mockExamService.getMockExamQuestions(mockExamId, authentication);
        log.info("Fetched {} questions for mockExamId={}", response.size(), mockExamId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{mockExamId}/questions/{questionId}")
    public ResponseEntity<String> removeQuestionFromMockExam(
            @PathVariable Long mockExamId,
            @PathVariable Long questionId) {
        log.info("Removing question from mock exam: mockExamId={} questionId={}", mockExamId, questionId);
        mockExamService.removeQuestionFromMockExam(mockExamId, questionId);
        log.info("Question removed from mock exam successfully: mockExamId={} questionId={}", mockExamId, questionId);
        return ResponseEntity.ok("Question removed from mock exam successfully");
    }

    @GetMapping("/{mockExamId}/available-questions")
    public ResponseEntity<List<QuestionResponseDTO>> getAvailableQuestions(@PathVariable Long mockExamId) {
        log.info("Fetching available questions for mockExamId={}", mockExamId);
        List<QuestionResponseDTO> response = mockExamService.getAvailableMockExamQuestions(mockExamId);
        log.info("Fetched {} available questions for mockExamId={}", response.size(), mockExamId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{mockExamId}/submit")
    public ResponseEntity<MockExamSubmitResponseDTO> submitMockExam(
            @PathVariable Long mockExamId,
            @RequestBody MockExamSubmitRequestDTO request,
            Authentication authentication) {
        log.info("Submitting mock exam: mockExamId={} user={}", mockExamId, authentication == null ? "anonymous" : authentication.getName());
        MockExamSubmitResponseDTO response = mockExamService.submitMockExam(mockExamId, request, authentication);
        log.info("Mock exam submission completed: mockExamId={} resultId={}", mockExamId, response.getMockExamResultId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{mockExamId}/results/{resultId}")
    public ResponseEntity<ExamReviewResponseDTO> getMockExamResultReview(
            @PathVariable Long mockExamId,
            @PathVariable Long resultId,
            Authentication authentication) {
        log.info("Fetching mock exam review: mockExamId={} resultId={} user={}", mockExamId, resultId, authentication == null ? "anonymous" : authentication.getName());
        ExamReviewResponseDTO response = mockExamService.getMockExamResultReview(mockExamId, resultId, authentication);
        log.info("Mock exam review fetched successfully: mockExamId={} resultId={}", mockExamId, resultId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results/me")
    public ResponseEntity<List<ExamAttemptSummaryDTO>> getMyMockExamAttempts(Authentication authentication) {
        log.info("Fetching my mock exam attempts for user={}", authentication == null ? "anonymous" : authentication.getName());
        List<ExamAttemptSummaryDTO> response = mockExamService.getMyMockExamAttempts(authentication);
        log.info("Fetched {} mock exam attempts for user={}", response.size(), authentication == null ? "anonymous" : authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{mockExamId}/results/{resultId}/attributes/{attributeId}/review-detail")
    public ResponseEntity<AttributeReviewDetailDTO> getAttributeReviewDetail(
            @PathVariable Long mockExamId,
            @PathVariable Long resultId,
            @PathVariable Long attributeId,
            @RequestParam Long questionId,
            Authentication authentication) {
        log.info("Fetching mock exam attribute review detail: mockExamId={} resultId={} attributeId={} questionId={} user={}", mockExamId, resultId, attributeId, questionId, authentication == null ? "anonymous" : authentication.getName());
        AttributeReviewDetailDTO response = mockExamService.getAttributeReviewDetail(mockExamId, resultId, questionId, attributeId, authentication);
        log.info("Mock exam attribute review detail fetched successfully: mockExamId={} resultId={} attributeId={}", mockExamId, resultId, attributeId);
        return ResponseEntity.ok(response);
    }
}
