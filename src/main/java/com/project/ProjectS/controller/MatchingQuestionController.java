package com.project.ProjectS.controller;

import com.project.ProjectS.model.MatchingQuestionRequestDTO;
import com.project.ProjectS.model.MatchingQuestionResponseDTO;
import com.project.ProjectS.model.QuestionExcelUploadRequestDTO;
import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import com.project.ProjectS.service.MatchingQuestionService;
import com.project.ProjectS.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/questions/matching")
public class MatchingQuestionController {

    @Autowired
    private MatchingQuestionService matchingQuestionService;

    @Autowired
    private QuestionService questionService;


    // CREATE
    @PostMapping
    public ResponseEntity<MatchingQuestionResponseDTO> createMatchingQuestion(
            @RequestBody MatchingQuestionRequestDTO request) {

        return ResponseEntity.ok(
                matchingQuestionService.createMatchingQuestion(request)
        );
    }


    // GET BY ID
    @GetMapping("/{questionId}")
    public ResponseEntity<MatchingQuestionResponseDTO> getMatchingQuestionById(
            @PathVariable Long questionId, Authentication authentication) {

        if (isLearner(authentication)) questionService.requireQuestionAccess(questionId, authentication.getName());

        return ResponseEntity.ok(
                matchingQuestionService.getMatchingQuestionById(questionId)
        );
    }


    // GET ALL
    @GetMapping
    public ResponseEntity<List<MatchingQuestionResponseDTO>> getAllMatchingQuestions(Authentication authentication) {

        List<MatchingQuestionResponseDTO> questions = matchingQuestionService.getAllMatchingQuestions();
        if (isLearner(authentication)) questions = questions.stream().filter(question ->
                questionService.isQuestionAllowedForUser(question.getQuestionId(), question.getCourseId(), authentication.getName())).toList();

        return ResponseEntity.ok(questions);
    }

    private boolean isLearner(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_STUDENT".equals(authority.getAuthority()) || "ROLE_GUEST".equals(authority.getAuthority()));
    }


    // UPDATE
    @PutMapping("/{questionId}")
    public ResponseEntity<MatchingQuestionResponseDTO> updateMatchingQuestion(
            @PathVariable Long questionId,
            @RequestBody MatchingQuestionRequestDTO request) {

        return ResponseEntity.ok(
                matchingQuestionService.updateMatchingQuestion(
                        questionId,
                        request
                )
        );
    }


    // DELETE
    @DeleteMapping("/{questionId}")
    public ResponseEntity<String> deleteMatchingQuestion(
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                matchingQuestionService.deleteMatchingQuestion(questionId)
        );
    }
    @PostMapping("/upload")
    public ResponseEntity<QuestionExcelUploadResponseDTO> uploadMatchingExcel(
            @RequestParam("file") MultipartFile file,
            @RequestPart("request") QuestionExcelUploadRequestDTO request) {

        return ResponseEntity.ok(
                matchingQuestionService.processMatchingExcel(
                        file,
                        request.getCourseId(),
                        request.getChapterId(),
                        request.getTopicId()
                )
        );
    }

}
