package com.project.ProjectS.controller;
import com.project.ProjectS.model.McqQuestionRequestDTO;
import com.project.ProjectS.model.McqQuestionResponseDTO;
import com.project.ProjectS.model.McqSubmissionRequestDTO;
import com.project.ProjectS.model.McqSubmissionResponseDTO;
import com.project.ProjectS.processor.McqExcelUploadProcessor;
import com.project.ProjectS.service.McqQuestionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            Long topicId) {

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
            @RequestBody McqSubmissionRequestDTO request) {

        McqSubmissionResponseDTO response =
                mcqQuestionService.submitMcqAnswers(
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/mcq/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<String> uploadMcqQuestions(

            @RequestPart("file")
            MultipartFile file,

            @RequestParam("courseId")
            Long courseId,

            @RequestParam("chapterId")
            Long chapterId,

            @RequestParam("topicId")
            Long topicId) {

        try {

            System.out.println();
            System.out.println("==========================================");
            System.out.println("MCQ EXCEL UPLOAD STARTED");
            System.out.println("==========================================");

            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Excel file is required");
            }

            if (courseId == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Course ID is required");
            }

            if (chapterId == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Chapter ID is required");
            }

            if (topicId == null) {

                return ResponseEntity
                        .badRequest()
                        .body("Topic ID is required");
            }

            System.out.println(
                    "File Name = "
                            + file.getOriginalFilename()
            );

            System.out.println(
                    "Course ID = "
                            + courseId
            );

            System.out.println(
                    "Chapter ID = "
                            + chapterId
            );

            System.out.println(
                    "Topic ID = "
                            + topicId
            );

            System.out.println("==========================================");

            int uploadedCount =
                    mcqExcelUploadProcessor.processExcel(
                            file,
                            courseId,
                            chapterId,
                            topicId
                    );

            System.out.println("==========================================");
            System.out.println("MCQ EXCEL UPLOAD COMPLETED");

            System.out.println(
                    "Uploaded Count = "
                            + uploadedCount
            );

            System.out.println("==========================================");

            return ResponseEntity.ok(
                    uploadedCount
                            + " MCQ questions uploaded successfully"
            );

        } catch (Exception e) {

            System.out.println("==========================================");
            System.out.println("MCQ EXCEL UPLOAD FAILED");

            System.out.println(
                    "Error = "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.out.println("==========================================");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "MCQ upload failed: "
                                    + e.getMessage()
                    );
        }
    }

}

