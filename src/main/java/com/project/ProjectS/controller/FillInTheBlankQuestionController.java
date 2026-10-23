package com.project.ProjectS.controller;

import com.project.ProjectS.model.FillInTheBlankQuestionRequestDTO;
import com.project.ProjectS.model.FillInTheBlankQuestionResponseDTO;
import com.project.ProjectS.model.QuestionExcelUploadRequestDTO;
import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import com.project.ProjectS.service.FillInTheBlankQuestionService;
import com.project.ProjectS.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/questions/fill-blank")
public class FillInTheBlankQuestionController {

    @Autowired
    private FillInTheBlankQuestionService fillInTheBlankQuestionService;

    @Autowired
    private QuestionService questionService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<FillInTheBlankQuestionResponseDTO>
    createFillInTheBlankQuestion(
            @RequestBody FillInTheBlankQuestionRequestDTO request) {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService
                        .createFillInTheBlankQuestion(request)
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @GetMapping("/{questionId}")
    public ResponseEntity<FillInTheBlankQuestionResponseDTO>
    getFillInTheBlankQuestionById(
            @PathVariable Long questionId, Authentication authentication) {

        if (isLearner(authentication)) questionService.requireQuestionAccess(questionId, authentication.getName());

        return ResponseEntity.ok(
                fillInTheBlankQuestionService
                        .getFillInTheBlankQuestionById(questionId)
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<List<FillInTheBlankQuestionResponseDTO>>
    getAllFillInTheBlankQuestions(Authentication authentication) {

        List<FillInTheBlankQuestionResponseDTO> questions = fillInTheBlankQuestionService.getAllFillInTheBlankQuestions();
        if (isLearner(authentication)) questions = questions.stream().filter(question ->
                questionService.isQuestionAllowedForUser(question.getQuestionId(), question.getCourseId(), authentication.getName())).toList();

        return ResponseEntity.ok(questions);
    }

    private boolean isLearner(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_STUDENT".equals(authority.getAuthority()) || "ROLE_GUEST".equals(authority.getAuthority()));
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{questionId}")
    public ResponseEntity<FillInTheBlankQuestionResponseDTO>
    updateFillInTheBlankQuestion(
            @PathVariable Long questionId,
            @RequestBody FillInTheBlankQuestionRequestDTO request) {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService
                        .updateFillInTheBlankQuestion(
                                questionId,
                                request
                        )
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{questionId}")
    public ResponseEntity<String>
    deleteFillInTheBlankQuestion(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                fillInTheBlankQuestionService
                        .deleteFillInTheBlankQuestion(
                                questionId
                        )
        );
    }


    // =========================================================
    // FILL-IN-THE-BLANKS EXCEL UPLOAD
    // =========================================================

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<QuestionExcelUploadResponseDTO>
    uploadFillInTheBlankQuestions(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request")
            QuestionExcelUploadRequestDTO request) {

        System.out.println(
                "Fill-in-the-Blanks Course ID = "
                        + request.getCourseId()
        );

        System.out.println(
                "Fill-in-the-Blanks Chapter ID = "
                        + request.getChapterId()
        );

        System.out.println(
                "Fill-in-the-Blanks Topic ID = "
                        + request.getTopicId()
        );

        QuestionExcelUploadResponseDTO response =
                questionService.uploadFillInTheBlankQuestions(
                        file,
                        request.getCourseId(),
                        request.getChapterId(),
                        request.getTopicId()
                );

        return ResponseEntity.ok(response);
    }
}
