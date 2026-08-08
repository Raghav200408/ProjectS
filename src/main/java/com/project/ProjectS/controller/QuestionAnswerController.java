package com.project.ProjectS.controller;

import com.project.ProjectS.model.QuestionAnswerRequestDTO;
import com.project.ProjectS.model.QuestionAnswerResponseDTO;
import com.project.ProjectS.service.QuestionAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question_answers")
public class QuestionAnswerController {

    @Autowired
    private QuestionAnswerService questionAnswerService;
    @PostMapping()
    public ResponseEntity<QuestionAnswerResponseDTO> saveAnswer(@RequestBody QuestionAnswerRequestDTO request)
    {
        return ResponseEntity.ok(
                questionAnswerService.saveAnswer(request)
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

    @PutMapping("/question/{questionId}/reset")
    public ResponseEntity<String> resetAnswersByQuestionId(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                questionAnswerService
                        .resetAnswersByQuestionId(questionId)
        );
    }

}
