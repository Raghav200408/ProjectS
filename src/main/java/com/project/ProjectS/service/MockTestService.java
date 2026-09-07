package com.project.ProjectS.service;

import com.project.ProjectS.entity.Question;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.McqQuestionRepository;
import com.project.ProjectS.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MockTestService {
    private static final int DEFAULT_QUESTION_COUNT = 20;
    private final QuestionRepository questions;
    private final McqQuestionRepository mcqs;
    private final McqQuestionService mcqQuestionService;
    private final SubscriptionEntitlementService entitlementService;

    public MockTestService(QuestionRepository questions, McqQuestionRepository mcqs,
                           McqQuestionService mcqQuestionService,
                           SubscriptionEntitlementService entitlementService) {
        this.questions = questions;
        this.mcqs = mcqs;
        this.mcqQuestionService = mcqQuestionService;
        this.entitlementService = entitlementService;
    }

    @Transactional
    public MockTestResponseDTO start(MockTestStartRequestDTO request, String email) {
        if (request.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID is required");
        }
        int requested = request.getQuestionCount() == null
                ? DEFAULT_QUESTION_COUNT : request.getQuestionCount();
        if (requested <= 0) {
            throw new IllegalArgumentException("Question count must be positive");
        }

        List<Question> candidates = new ArrayList<>(questions.findByActiveRowTrue().stream()
                .filter(question -> question.getCourse().getCourseId().equals(request.getCourseId()))
                .filter(question -> mcqs.findById(question.getQuestionId())
                        .map(mcq -> Boolean.TRUE.equals(mcq.getActiveRow())).orElse(false))
                .toList());
        Collections.shuffle(candidates);
        int count = Math.min(requested, candidates.size());
        if (count == 0) {
            throw new IllegalStateException("No active MCQ questions are available for this course");
        }
        Long userId = entitlementService.requireCourseAccess(email, request.getCourseId())
                .getUser().getUserId();
        entitlementService.consumeMockTest(userId, request.getCourseId());

        MockTestResponseDTO response = new MockTestResponseDTO();
        response.setCourseId(request.getCourseId());
        response.setQuestionCount(count);
        response.setQuestions(candidates.subList(0, count).stream()
                .map(question -> mcqQuestionService.getMcqQuestionById(question.getQuestionId()))
                .peek(question -> question.getOptions().forEach(option -> option.setIsCorrect(null)))
                .toList());
        return response;
    }
}
