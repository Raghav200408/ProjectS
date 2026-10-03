package com.project.ProjectS.controller;

import com.project.ProjectS.model.QuestionAnswerRequestDTO;
import com.project.ProjectS.model.QuestionAnswerResponseDTO;
import com.project.ProjectS.service.QuestionAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question_answers")
public class QuestionAnswerController {
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
        return ResponseEntity.ok(
                questionAnswerService.saveAnswer(request, authentication)
        );
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<QuestionAnswerResponseDTO>>
    getAnswersByQuestionId(
            @PathVariable Long questionId) {

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

        return ResponseEntity.ok(
                questionAnswerService
                        .getAnswersByUserAndQuestion(
                                questionAnswerService.getAuthenticatedUserId(authentication),
                                questionId
                        )
        );
    }

    @PutMapping("/user/{userId}/question/{questionId}/reset")
    public ResponseEntity<String> resetAnswersByUserAndQuestion(
            @PathVariable("userId") Long ignoredUserId,
            @PathVariable Long questionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                questionAnswerService
                        .resetAnswersByUserAndQuestion(
                                questionAnswerService.getAuthenticatedUserId(authentication),
                                questionId
                        )
        );
    }

}

