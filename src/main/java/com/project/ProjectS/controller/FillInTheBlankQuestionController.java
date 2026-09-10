package com.project.ProjectS.controller;

import com.project.ProjectS.model.FillInTheBlankQuestionRequestDTO;
import com.project.ProjectS.model.FillInTheBlankQuestionResponseDTO;
import com.project.ProjectS.service.FillInTheBlankQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions/fill-blank")
public class FillInTheBlankQuestionController {

    @Autowired
    private FillInTheBlankQuestionService fillInTheBlankQuestionService;


    // CREATE
    @PostMapping
    public ResponseEntity<FillInTheBlankQuestionResponseDTO> createFillInTheBlankQuestion(
            @RequestBody FillInTheBlankQuestionRequestDTO request) {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService.createFillInTheBlankQuestion(request)
        );
    }


    // GET BY ID
    @GetMapping("/{questionId}")
    public ResponseEntity<FillInTheBlankQuestionResponseDTO> getFillInTheBlankQuestionById(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService.getFillInTheBlankQuestionById(questionId)
        );
    }


    // GET ALL
    @GetMapping
    public ResponseEntity<List<FillInTheBlankQuestionResponseDTO>>
    getAllFillInTheBlankQuestions() {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService.getAllFillInTheBlankQuestions()
        );
    }


    // UPDATE
    @PutMapping("/{questionId}")
    public ResponseEntity<FillInTheBlankQuestionResponseDTO> updateFillInTheBlankQuestion(
            @PathVariable Long questionId,
            @RequestBody FillInTheBlankQuestionRequestDTO request) {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService.updateFillInTheBlankQuestion(
                        questionId,
                        request
                )
        );
    }


    // DELETE
    @DeleteMapping("/{questionId}")
    public ResponseEntity<String> deleteFillInTheBlankQuestion(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService.deleteFillInTheBlankQuestion(
                        questionId
                )
        );
    }
}