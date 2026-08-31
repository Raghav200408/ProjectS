package com.project.ProjectS.controller;

import com.project.ProjectS.model.McqQuestionRequestDTO;
import com.project.ProjectS.model.McqQuestionResponseDTO;
import com.project.ProjectS.model.McqSubmissionRequestDTO;
import com.project.ProjectS.model.McqSubmissionResponseDTO;
import com.project.ProjectS.service.McqQuestionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcq-questions")
@CrossOrigin(origins = "*")
public class McqQuestionController {

    private final McqQuestionService mcqQuestionService;

    public McqQuestionController(
            McqQuestionService mcqQuestionService
    ) {
        this.mcqQuestionService = mcqQuestionService;
    }


    // 1. CREATE MCQ
    @PostMapping
    public ResponseEntity<McqQuestionResponseDTO> createMcqQuestion(
            @RequestBody McqQuestionRequestDTO request
    ) {

        McqQuestionResponseDTO response =
                mcqQuestionService.createMcqQuestion(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }


    // 2. GET MCQ BY QUESTION ID
    @GetMapping("/{questionId}")
    public ResponseEntity<McqQuestionResponseDTO> getMcqQuestionById(
            @PathVariable Long questionId
    ) {

        McqQuestionResponseDTO response =
                mcqQuestionService.getMcqQuestionById(questionId);

        return ResponseEntity.ok(response);
    }


    // 3. GET ALL MCQs
    @GetMapping
    public ResponseEntity<List<McqQuestionResponseDTO>> getAllMcqQuestions() {

        List<McqQuestionResponseDTO> response =
                mcqQuestionService.getAllMcqQuestions();

        return ResponseEntity.ok(response);
    }


    // 4. GET MCQs BY COURSE, CHAPTER AND CATEGORY
    @GetMapping("/filter")
    public ResponseEntity<List<McqQuestionResponseDTO>> getMcqQuestionsByFilter(

            @RequestParam Long courseId,

            @RequestParam Long chapterId,

            @RequestParam Long categoryId
    ) {

        List<McqQuestionResponseDTO> response =
                mcqQuestionService.getMcqQuestionsByFilter(
                        courseId,
                        chapterId,
                        categoryId
                );

        return ResponseEntity.ok(response);
    }


    // 5. UPDATE MCQ
    @PutMapping("/{questionId}")
    public ResponseEntity<McqQuestionResponseDTO> updateMcqQuestion(

            @PathVariable Long questionId,

            @RequestBody McqQuestionRequestDTO request
    ) {

        McqQuestionResponseDTO response =
                mcqQuestionService.updateMcqQuestion(
                        questionId,
                        request
                );

        return ResponseEntity.ok(response);
    }


    // 6. DELETE MCQ
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteMcqQuestion(
            @PathVariable Long questionId
    ) {

        mcqQuestionService.deleteMcqQuestion(questionId);

        return ResponseEntity.noContent().build();
    }


    // 7. SUBMIT MCQ ANSWERS
    @PostMapping("/submit")
    public ResponseEntity<McqSubmissionResponseDTO> submitMcqAnswers(
            @RequestBody McqSubmissionRequestDTO request
    ) {

        McqSubmissionResponseDTO response =
                mcqQuestionService.submitMcqAnswers(request);

        return ResponseEntity.ok(response);
    }
}