package com.project.ProjectS.controller;
import com.project.ProjectS.model.McqQuestionRequestDTO;
import com.project.ProjectS.model.McqQuestionResponseDTO;
import com.project.ProjectS.model.McqSubmissionRequestDTO;
import com.project.ProjectS.model.McqSubmissionResponseDTO;
import com.project.ProjectS.processor.McqExcelUploadProcessor;
import com.project.ProjectS.service.McqQuestionService;

import com.project.ProjectS.model.QuestionExcelUploadRequestDTO;
import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/mcq-questions")
@CrossOrigin(origins = "*")
public class McqQuestionController {
    private final McqQuestionService mcqQuestionService;
    private final McqExcelUploadProcessor mcqExcelUploadProcessor;

    public McqQuestionController(
            McqQuestionService mcqQuestionService,
            McqExcelUploadProcessor mcqExcelUploadProcessor) {

        this.mcqQuestionService =
                mcqQuestionService;

        this.mcqExcelUploadProcessor =
                mcqExcelUploadProcessor;
    }

    @PostMapping
    public ResponseEntity<McqQuestionResponseDTO> createMcqQuestion(
            @RequestBody McqQuestionRequestDTO request) {

        McqQuestionResponseDTO response =
                mcqQuestionService.createMcqQuestion(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<McqQuestionResponseDTO> getMcqQuestionById(
            @PathVariable Long questionId) {

        McqQuestionResponseDTO response =
                mcqQuestionService.getMcqQuestionById(
                        questionId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<McqQuestionResponseDTO>>
    getAllMcqQuestions() {

        List<McqQuestionResponseDTO> response =
                mcqQuestionService.getAllMcqQuestions();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<McqQuestionResponseDTO>>
    getMcqQuestionsByFilter(

            @RequestParam("courseId")
            Long courseId,

            @RequestParam("chapterId")
            Long chapterId,

            @RequestParam("topicId")
            Long topicId,
            Authentication authentication) {

//        if (authentication != null) {
//            mcqQuestionService.requireCourseAccess(courseId, authentication.getName());
//        }

        List<McqQuestionResponseDTO> response =
                mcqQuestionService.getMcqQuestionsByFilter(
                        courseId,
                        chapterId,
                        topicId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<McqQuestionResponseDTO>
    updateMcqQuestion(

            @PathVariable Long questionId,

            @RequestBody McqQuestionRequestDTO request) {

        McqQuestionResponseDTO response =
                mcqQuestionService.updateMcqQuestion(
                        questionId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteMcqQuestion(
            @PathVariable Long questionId) {

        mcqQuestionService.deleteMcqQuestion(
                questionId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/submit")
    public ResponseEntity<McqSubmissionResponseDTO>
    submitMcqAnswers(
            @RequestBody McqSubmissionRequestDTO request,
            Authentication authentication) {

        McqSubmissionResponseDTO response =
                mcqQuestionService.submitMcqAnswersAsAuthenticated(
                        request, authentication.getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/mcq/upload", consumes = "multipart/form-data")
    public ResponseEntity<QuestionExcelUploadResponseDTO> uploadMcqQuestions(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "request", required = false) QuestionExcelUploadRequestDTO request,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "chapterId", required = false) Long chapterId,
            @RequestParam(value = "topicId", required = false) Long topicId) throws java.io.IOException {
        // Retain legacy flat parameters while using the shared request for current clients.
        if (request != null) {
            courseId = request.getCourseId() == null ? null : request.getCourseId().longValue();
            chapterId = request.getChapterId() == null ? null : request.getChapterId().longValue();
            topicId = request.getTopicId() == null ? null : request.getTopicId().longValue();
        }
        try {
            return ResponseEntity.ok(mcqExcelUploadProcessor.processExcel(file, courseId, chapterId, topicId));
        } catch (IllegalArgumentException ex) {
            QuestionExcelUploadResponseDTO result = new QuestionExcelUploadResponseDTO();
            result.setSuccess(false);
            result.setMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
