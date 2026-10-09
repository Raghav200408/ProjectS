package com.project.ProjectS.controller;

import com.project.ProjectS.model.QuestionAnswerRequestDTO;
import com.project.ProjectS.model.QuestionAnswerResponseDTO;
import com.project.ProjectS.service.QuestionAnswerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question_answers")
public class QuestionAnswerController {
    private static final Logger log = LogManager.getLogger(QuestionAnswerController.class);

    @Autowired
    public QuestionAnswerController(QuestionAnswerService questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    private final QuestionAnswerService questionAnswerService;
    @PostMapping()
    public ResponseEntity<QuestionAnswerResponseDTO> saveAnswer(
            @RequestBody QuestionAnswerRequestDTO request,
            Authentication authentication)
    {
        log.info("POST /api/question_answers request for userId={} questionId={}",
                request != null ? request.getUserId() : null,
                request != null ? request.getQuestionId() : null);
        return ResponseEntity.ok(
                questionAnswerService.saveAnswer(request, authentication)
        );
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<QuestionAnswerResponseDTO>>
    getAnswersByQuestionId(
            @PathVariable Long questionId) {
        log.info("GET answers by questionId={}", questionId);
        return ResponseEntity.ok(
                questionAnswerService
                        .getAnswersByQuestionId(questionId)
        );
    }
    @GetMapping("/user/{userId}/question/{questionId}")
    public ResponseEntity<List<QuestionAnswerResponseDTO>>
    getAnswersByUserAndQuestion(
            @PathVariable("userId") Long ignoredUserId,
            @PathVariable Long questionId,
            Authentication authentication) {
        Long userId = questionAnswerService.getAuthenticatedUserId(authentication);
        log.info("GET user answers for userId={} questionId={}", userId, questionId);
        return ResponseEntity.ok(
                questionAnswerService
                        .getAnswersByUserAndQuestion(
                                userId,
                                questionId
                        )
        );
    }

    @PutMapping("/user/{userId}/question/{questionId}/reset")
    public ResponseEntity<String> resetAnswersByUserAndQuestion(
            @PathVariable("userId") Long ignoredUserId,
            @PathVariable Long questionId,
            Authentication authentication) {
        Long userId = questionAnswerService.getAuthenticatedUserId(authentication);
        log.info("RESET answers for userId={} questionId={}", userId, questionId);
        return ResponseEntity.ok(
                questionAnswerService
                        .resetAnswersByUserAndQuestion(
                                userId,
                                questionId
                        )
        );
    }

}

