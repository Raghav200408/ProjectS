package com.project.ProjectS.controller;

import com.project.ProjectS.model.AddExamQuestionsRequestDTO;
import com.project.ProjectS.model.ExamRequestDTO;
import com.project.ProjectS.model.ExamResponseDTO;
import com.project.ProjectS.model.QuestionResponseDTO;
import com.project.ProjectS.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }


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

        return ResponseEntity.ok(
                examService.getAllExams()
        );
    }


    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponseDTO> getExamById(
            @PathVariable Long examId) {

        return ResponseEntity.ok(
                examService.getExamById(examId)
        );
    }


    @PutMapping("/{examId}")
    public ResponseEntity<ExamResponseDTO> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequestDTO request) {

        return ResponseEntity.ok(
                examService.updateExam(
                        examId,
                        request
                )
        );
    }


    @DeleteMapping("/{examId}")
    public ResponseEntity<String> deleteExam(
            @PathVariable Long examId) {

        examService.deleteExam(examId);

        return ResponseEntity.ok(
                "Exam deleted successfully"
        );
    }


    @PostMapping("/{examId}/questions")
    public ResponseEntity<String> addQuestionsToExam(
            @PathVariable Long examId,
            @Valid @RequestBody AddExamQuestionsRequestDTO request) {

        examService.addQuestionsToExam(
                examId,
                request
        );

        return ResponseEntity.ok(
                "Questions added to exam successfully"
        );
    }


    @GetMapping("/{examId}/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getExamQuestions(
            @PathVariable Long examId) {

        return ResponseEntity.ok(
                examService.getExamQuestions(examId)
        );
    }


    @DeleteMapping("/{examId}/questions/{questionId}")
    public ResponseEntity<String> removeQuestionFromExam(
            @PathVariable Long examId,
            @PathVariable Long questionId) {

        examService.removeQuestionFromExam(
                examId,
                questionId
        );

        return ResponseEntity.ok(
                "Question removed from exam successfully"
        );
    }

    @GetMapping("/{examId}/available-questions")
    public ResponseEntity<List<QuestionResponseDTO>> getAvailableQuestions(
            @PathVariable Long examId) {

        return ResponseEntity.ok(
                examService.getAvailableQuestions(examId)
        );
    }
}