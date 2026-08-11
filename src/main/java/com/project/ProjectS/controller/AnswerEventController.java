package com.project.ProjectS.controller;

import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.service.AnswerEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/answer_events")
public class AnswerEventController {

    @Autowired
    private AnswerEventService answerEventService;



    // CREATE EVENT
    @PostMapping
    public ResponseEntity<AnswerEventResponseDTO>
    createEvent(
            @RequestBody AnswerEventRequestDTO request) {

        return ResponseEntity.ok(
                answerEventService.createEvent(request)
        );
    }

    // GET ALL
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
            @PathVariable Long userId,
            @PathVariable Long questionId,
            @PathVariable Long attributeId) {

        return ResponseEntity.ok(
                answerEventService
                        .getByUserQuestionAttribute(
                                userId,
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
            @PathVariable Long userId,
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                answerEventService.getMistakes(
                        userId,
                        questionId
                )
        );
    }
    @GetMapping("/user/{userId}/mistakes")
    public ResponseEntity<List<AnswerEventResponseDTO>>
    getAllMistakesByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                answerEventService.getAllMistakesByUser(userId)
        );
    }
    @GetMapping("/user/{userId}/marks")
    public ResponseEntity<BigDecimal> getOverallMarks(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                answerEventService.getOverallMarks(userId)
        );
    }
}