package com.project.ProjectS.controller;

import com.project.ProjectS.entity.Question;
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
    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    private final QuestionService questionService;


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
    @GetMapping("/QuestionText")
    public ResponseEntity<List<QuestionResponseDTO>> getAllQuestionText(){
        List<QuestionResponseDTO> questions =
                questionService.getAllQuestionText();

        return ResponseEntity.ok(questions);
    }
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
    @DeleteMapping("/{questionId}")
    public ResponseEntity<String> deleteQuestion(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                questionService.deleteQuestion(questionId)
        );
    }
    @PostMapping("/upload")
    public ResponseEntity<QuestionExcelUploadResponseDTO> uploadQuestions(
            @RequestParam("file") MultipartFile file) {

        QuestionExcelUploadResponseDTO response =
                questionService.uploadQuestions(file);

        return ResponseEntity.ok(response);
    }


}
