package com.project.ProjectS.controller;

import com.project.ProjectS.model.*;
import com.project.ProjectS.service.ExamService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    private static final Logger log = LogManager.getLogger(ExamController.class);

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    public ResponseEntity<ExamResponseDTO> createExam(
            @Valid @RequestBody ExamRequestDTO request) {
        log.info("Creating exam: examName={} courseId={}", request.getExamName(), request.getCourseId());
        ExamResponseDTO response = examService.createExam(request);
        log.info("Exam created successfully: examId={}", response.getExamId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExamResponseDTO>> getAllExams(Authentication auth) {
        log.info("Fetching all exams for user={}", auth == null ? "anonymous" : auth.getName());
        List<ExamResponseDTO> response = examService.getAllExams(auth);
        log.info("Fetched {} exams", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponseDTO> getExamById(@PathVariable Long examId) {
        log.info("Fetching exam by id={}", examId);
        ExamResponseDTO response = examService.getExamById(examId);
        log.info("Exam fetched successfully: examId={}", examId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{examId}")
    public ResponseEntity<ExamResponseDTO> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequestDTO request) {
        log.info("Updating exam: examId={} examName={}", examId, request.getExamName());
        ExamResponseDTO response = examService.updateExam(examId, request);
        log.info("Exam updated successfully: examId={}", examId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<String> deleteExam(@PathVariable Long examId) {
        log.info("Deleting exam: examId={}", examId);
        examService.deleteExam(examId);
        log.info("Exam deleted successfully: examId={}", examId);
        return ResponseEntity.ok("Exam deleted successfully");
    }

    @PostMapping("/{examId}/questions")
    public ResponseEntity<String> addQuestionsToExam(
            @PathVariable Long examId,
            @Valid @RequestBody AddExamQuestionsRequestDTO request) {
        log.info("Adding questions to exam: examId={} questionCount={}", examId, request.getQuestionIds() == null ? 0 : request.getQuestionIds().size());
        examService.addQuestionsToExam(examId, request);
        log.info("Questions added to exam successfully: examId={}", examId);
        return ResponseEntity.ok("Questions added to exam successfully");
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getExamQuestions(
            @PathVariable Long examId,
            Authentication authentication) {
        log.info("Fetching exam questions: examId={} user={}", examId, authentication == null ? "anonymous" : authentication.getName());
        List<QuestionResponseDTO> response = examService.getExamQuestions(examId, authentication);
        log.info("Fetched {} questions for examId={}", response.size(), examId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{examId}/questions/{questionId}")
    public ResponseEntity<String> removeQuestionFromExam(
            @PathVariable Long examId,
            @PathVariable Long questionId) {
        log.info("Removing question from exam: examId={} questionId={}", examId, questionId);
        examService.removeQuestionFromExam(examId, questionId);
        log.info("Question removed from exam successfully: examId={} questionId={}", examId, questionId);
        return ResponseEntity.ok("Question removed from exam successfully");
    }

    @GetMapping("/{examId}/available-questions")
    public ResponseEntity<List<QuestionResponseDTO>> getAvailableQuestions(@PathVariable Long examId) {
        log.info("Fetching available questions for examId={}", examId);
        List<QuestionResponseDTO> response = examService.getAvailableQuestions(examId);
        log.info("Fetched {} available questions for examId={}", response.size(), examId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{examId}/submit")
    public ResponseEntity<ExamSubmitResponseDTO> submitExam(
            @PathVariable Long examId,
            @RequestBody ExamSubmitRequestDTO request,
            Authentication authentication) {
        log.info("Submitting exam: examId={} user={}", examId, authentication == null ? "anonymous" : authentication.getName());
        ExamSubmitResponseDTO response = examService.submitExam(examId, request, authentication);
        log.info("Exam submission completed: examId={} resultId={}", examId, response.getExamResultId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}/results/{resultId}")
    public ResponseEntity<ExamReviewResponseDTO> getExamResultReview(
            @PathVariable Long examId,
            @PathVariable Long resultId,
            Authentication authentication) {
        log.info("Fetching exam review: examId={} resultId={} user={}", examId, resultId, authentication == null ? "anonymous" : authentication.getName());
        ExamReviewResponseDTO response = examService.getExamResultReview(examId, resultId, authentication);
        log.info("Exam review fetched successfully: examId={} resultId={}", examId, resultId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/results/me")
    public ResponseEntity<List<ExamAttemptSummaryDTO>> getMyExamAttempts(Authentication authentication) {
        log.info("Fetching my exam attempts for user={}", authentication == null ? "anonymous" : authentication.getName());
        List<ExamAttemptSummaryDTO> response = examService.getMyExamAttempts(authentication);
        log.info("Fetched {} exam attempts for user={}", response.size(), authentication == null ? "anonymous" : authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}/results/{resultId}/attributes/{attributeId}/review-detail")
    public ResponseEntity<AttributeReviewDetailDTO> getAttributeReviewDetail(
            @PathVariable Long examId,
            @PathVariable Long resultId,
            @PathVariable Long attributeId,
            @RequestParam Long questionId,
            Authentication authentication) {
        log.info("Fetching attribute review detail: examId={} resultId={} attributeId={} questionId={} user={}", examId, resultId, attributeId, questionId, authentication == null ? "anonymous" : authentication.getName());
        AttributeReviewDetailDTO response = examService.getAttributeReviewDetail(examId, resultId, questionId, attributeId, authentication);
        log.info("Attribute review detail fetched successfully: examId={} resultId={} attributeId={}", examId, resultId, attributeId);
        return ResponseEntity.ok(response);
    }
}