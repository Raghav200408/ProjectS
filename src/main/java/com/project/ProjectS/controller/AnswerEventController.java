package com.project.ProjectS.controller;

import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.service.AnswerEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/answer_events")
public class AnswerEventController {
    @Autowired
    public AnswerEventController(AnswerEventService answerEventService) {
        this.answerEventService = answerEventService;
    }

    private final AnswerEventService answerEventService;



    // CREATE EVENT
    @PostMapping
    public ResponseEntity<AnswerEventResponseDTO>
    createEvent(
            @RequestBody AnswerEventRequestDTO request,
            Authentication authentication) {

        return ResponseEntity.ok(
                answerEventService.createEvent(request, authentication)
        );
    }

    // GET ALL
    @GetMapping("/user/{userId}/question/{questionId}")
    public ResponseEntity<List<AnswerEventResponseDTO>> getCurrentPracticeEvents(
            @PathVariable("userId") Long ignoredUserId,
            @PathVariable Long questionId, Authentication authentication) {
        return ResponseEntity.ok(answerEventService.getCurrentPracticeEvents(
                answerEventService.getAuthenticatedUserId(authentication), questionId));
    }

    @GetMapping
    public ResponseEntity<List<AnswerEventResponseDTO>>
    getAllEvents() {

        return ResponseEntity.ok(
                answerEventService.getAllEvents()
        );
    }
    // GET BY ID
    @GetMapping("/{answerEventId}")
    public ResponseEntity<AnswerEventResponseDTO>
    getById(
            @PathVariable Long answerEventId) {

        return ResponseEntity.ok(
                answerEventService.getById(
                        answerEventId
                )
        );
    }

    // GET USER + QUESTION + ATTRIBUTE
    @GetMapping(
            "/user/{userId}/question/{questionId}/attribute/{attributeId}"
    )
    public ResponseEntity<List<AnswerEventResponseDTO>>
    getByUserQuestionAttribute(
            @PathVariable("userId") Long ignoredUserId,
            @PathVariable Long questionId,
            @PathVariable Long attributeId,
            Authentication authentication) {

        return ResponseEntity.ok(
                answerEventService
                        .getByUserQuestionAttribute(
                                answerEventService.getAuthenticatedUserId(authentication),
                                questionId,
                                attributeId
                        )
        );
    }


    //check mistakes
    @GetMapping(
            "/user/{userId}/question/{questionId}/mistakes"
    )
    public ResponseEntity<List<AnswerEventResponseDTO>>
    getMistakes(
            @PathVariable("userId") Long ignoredUserId,
            @PathVariable Long questionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                answerEventService.getMistakes(
                        answerEventService.getAuthenticatedUserId(authentication),
                        questionId
                )
        );
    }
    @GetMapping("/user/{userId}/mistakes")
    public ResponseEntity<List<AnswerEventResponseDTO>>
    getAllMistakesByUser(
            @PathVariable("userId") Long ignoredUserId,
            Authentication authentication) {

        return ResponseEntity.ok(
                answerEventService.getAllMistakesByUser(
                        answerEventService.getAuthenticatedUserId(authentication))
        );
    }
    @GetMapping("/user/{userId}/marks")
    public ResponseEntity<BigDecimal> getOverallMarks(
            @PathVariable("userId") Long ignoredUserId,
            Authentication authentication) {

        return ResponseEntity.ok(
                answerEventService.getOverallMarks(
                        answerEventService.getAuthenticatedUserId(authentication))
        );
    }
    @PutMapping("/user/{userId}/question/{questionId}/reset")
    public ResponseEntity<String> resetEvents(
            @PathVariable("userId") Long ignoredUserId,
            @PathVariable Long questionId,
            Authentication authentication) {

        int count = answerEventService.resetEvents(
                answerEventService.getAuthenticatedUserId(authentication),
                questionId
        );

        return ResponseEntity.ok(
                count + " answer events reset successfully"
        );
    }
}
