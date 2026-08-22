package com.project.ProjectS.controller;

import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import com.project.ProjectS.model.QuestionRequestDTO;
import com.project.ProjectS.model.QuestionResponseDTO;
import com.project.ProjectS.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // CREATE QUESTION
    @PostMapping
    public ResponseEntity<QuestionResponseDTO> createQuestion(
            @RequestBody QuestionRequestDTO request) {

        QuestionResponseDTO response =
                questionService.createQuestion(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    // GET ALL QUESTIONS
    @GetMapping
    public ResponseEntity<List<QuestionResponseDTO>> getAllQuestions() {

        List<QuestionResponseDTO> questions =
                questionService.getAllQuestions();

        return ResponseEntity.ok(questions);
    }

    // GET QUESTION BY ID
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponseDTO> getQuestionById(
            @PathVariable Long questionId) {

        QuestionResponseDTO question =
                questionService.getQuestionById(questionId);

        return ResponseEntity.ok(question);
    }

    // GET ALL QUESTION TEXT
    @GetMapping("/QuestionText")
    public ResponseEntity<List<QuestionResponseDTO>> getAllQuestionText() {

        List<QuestionResponseDTO> questions =
                questionService.getAllQuestionText();

        return ResponseEntity.ok(questions);
    }

    // UPDATE QUESTION
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionRequestDTO request) {

        QuestionResponseDTO response =
                questionService.updateQuestion(
                        questionId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // DELETE QUESTION
    @DeleteMapping("/{questionId}")
    public ResponseEntity<String> deleteQuestion(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                questionService.deleteQuestion(questionId)
        );
    }

    // EXCEL UPLOAD
    @PostMapping("/upload")
    public ResponseEntity<QuestionExcelUploadResponseDTO> uploadQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Integer courseId,
            @RequestParam("chapterId") Integer chapterId,
            @RequestParam("categoryId") Integer categoryId) {
        System.out.println("course id "+courseId);
        System.out.println("category id "+categoryId);
        System.out.println("chapter id "+chapterId);
        QuestionExcelUploadResponseDTO response =
                questionService.uploadQuestions(
                        file,
                        courseId,
                        chapterId,
                        categoryId
                );

        return ResponseEntity.ok(response);
    }

    // FILTER QUESTIONS
    @GetMapping("/filter")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestionsByMapping(
            @RequestParam Long courseId,
            @RequestParam Long chapterId,
            @RequestParam Long categoryId) {

        List<QuestionResponseDTO> questions =
                questionService.getQuestionsByMapping(
                        courseId,
                        chapterId,
                        categoryId
                );

        return ResponseEntity.ok(questions);
    }
}