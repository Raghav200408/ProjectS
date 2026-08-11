package com.project.ProjectS.controller;

import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.service.AnswerEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answer-events")
public class AnswerEventController {

    @Autowired
    private AnswerEventService answerEventService;


    // Record one user answer activity
    @PostMapping
    public ResponseEntity<AnswerEventResponseDTO> processAnswer(
            @RequestBody AnswerEventRequestDTO request) {

        return ResponseEntity.ok(
                answerEventService.processAnswer(request)
        );
    }


    // Get all answer activities for a question
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerEventResponseDTO>> getEventsByQuestionId(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                answerEventService.getEventsByQuestionId(questionId)
        );
    }


    // Get only mistakes for a question
    @GetMapping("/question/{questionId}/mistakes")
    public ResponseEntity<List<AnswerEventResponseDTO>> getMistakesByQuestionId(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                answerEventService.getMistakesByQuestionId(questionId)
        );
    }
}