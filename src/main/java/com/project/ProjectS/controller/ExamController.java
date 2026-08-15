package com.project.ProjectS.controller;

import com.project.ProjectS.model.ExamRequestDTO;
import com.project.ProjectS.model.ExamResponseDTO;
import com.project.ProjectS.model.QuestionFilterRequestDTO;
import com.project.ProjectS.model.QuestionResponseDTO;
import com.project.ProjectS.service.ExamService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    private final ExamService examService;
    @PostMapping
    public ResponseEntity<ExamResponseDTO> createExam(
            @Valid @RequestBody ExamRequestDTO request) {

        ExamResponseDTO response =
                examService.createExam(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<List<ExamResponseDTO>> getAllExams() {

        List<ExamResponseDTO> exams =
                examService.getAllExams();

        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponseDTO> getExamById(
            @PathVariable Long examId) {

        ExamResponseDTO response =
                examService.getExamById(examId);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{examId}")
    public ResponseEntity<ExamResponseDTO> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequestDTO request) {

        ExamResponseDTO response =
                examService.updateExam(examId, request);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{examId}")
    public ResponseEntity<String> deleteExam(
            @PathVariable Long examId) {

        String response =
                examService.deleteExam(examId);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{examId}/restore")
    public ResponseEntity<String> restoreExam(
            @PathVariable Long examId) {

        return ResponseEntity.ok(
                examService.restoreExam(examId)
        );
    }
    @PostMapping("/questions/filter")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestionsByFilter(
            @Valid @RequestBody QuestionFilterRequestDTO request) {

        List<QuestionResponseDTO> response =
                examService.getQuestionsByFilter(request);

        return ResponseEntity.ok(response);
    }
}
