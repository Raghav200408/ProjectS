package com.project.ProjectS.controller;

import com.project.ProjectS.model.MatchingQuestionRequestDTO;
import com.project.ProjectS.model.MatchingQuestionResponseDTO;
import com.project.ProjectS.service.MatchingQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions/matching")
public class MatchingQuestionController {

    @Autowired
    private MatchingQuestionService matchingQuestionService;


    // CREATE
    @PostMapping
    public ResponseEntity<MatchingQuestionResponseDTO> createMatchingQuestion(
            @RequestBody MatchingQuestionRequestDTO request) {

        return ResponseEntity.ok(
                matchingQuestionService.createMatchingQuestion(request)
        );
    }


    // GET BY ID
    @GetMapping("/{questionId}")
    public ResponseEntity<MatchingQuestionResponseDTO> getMatchingQuestionById(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                matchingQuestionService.getMatchingQuestionById(questionId)
        );
    }


    // GET ALL
    @GetMapping
    public ResponseEntity<List<MatchingQuestionResponseDTO>> getAllMatchingQuestions() {

        return ResponseEntity.ok(
                matchingQuestionService.getAllMatchingQuestions()
        );
    }


    // UPDATE
    @PutMapping("/{questionId}")
    public ResponseEntity<MatchingQuestionResponseDTO> updateMatchingQuestion(
            @PathVariable Long questionId,
            @RequestBody MatchingQuestionRequestDTO request) {

        return ResponseEntity.ok(
                matchingQuestionService.updateMatchingQuestion(
                        questionId,
                        request
                )
        );
    }


    // DELETE
    @DeleteMapping("/{questionId}")
    public ResponseEntity<String> deleteMatchingQuestion(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                matchingQuestionService.deleteMatchingQuestion(questionId)
        );
    }
}