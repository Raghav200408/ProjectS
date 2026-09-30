package com.project.ProjectS.service;

import com.project.ProjectS.entity.PracticeResult;
import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionAttribute;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.model.PracticeResultRequestDTO;
import com.project.ProjectS.model.PracticeResultResponseDTO;
import com.project.ProjectS.repository.McqQuestionRepository;
import com.project.ProjectS.repository.PracticeResultRepository;
import com.project.ProjectS.repository.QuestionAttributeRepository;
import com.project.ProjectS.repository.QuestionRepository;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Keeps practice_results - the CURRENT state of each practice unit - in step
 * with the historical answer_events.
 *
 * Practice only: exam and mock-exam attempts never come through here, and the
 * answer_events rows this writes always have exam_id / mock_exam_id NULL.
 */
@Service
public class PracticeResultService {

    private static final String TYPE_ATTRIBUTE = "ATTRIBUTE";
    private static final String TYPE_MCQ = "MCQ";

    private static final Set<String> EVENT_TYPES =
            Set.of("ANSWER", "HINT", "AUTOFILL");

    private final PracticeResultRepository practiceResultRepository;
    private final AnswerEventService answerEventService;
    private final QuestionRepository questionRepository;
    private final QuestionAttributeRepository questionAttributeRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final UserRepository userRepository;

    @Autowired
    public PracticeResultService(
            PracticeResultRepository practiceResultRepository,
            AnswerEventService answerEventService,
            QuestionRepository questionRepository,
            QuestionAttributeRepository questionAttributeRepository,
            McqQuestionRepository mcqQuestionRepository,
            UserRepository userRepository) {

        this.practiceResultRepository = practiceResultRepository;
        this.answerEventService = answerEventService;
        this.questionRepository = questionRepository;
        this.questionAttributeRepository = questionAttributeRepository;
        this.mcqQuestionRepository = mcqQuestionRepository;
        this.userRepository = userRepository;
    }


    /**
     * POST /api/practice/results - one attempted attribute/unit.
     *
     * One transaction: the historical answer_events row and the current
     * practice_results row are both written, or neither is.
     */
    @Transactional
    public PracticeResultResponseDTO recordAttributeResult(
            PracticeResultRequestDTO request,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        if (request == null) {
            throw badRequest("Request body is required");
        }

        // --- structure -----------------------------------------------------
        require(request.getQuestionId(), "questionId");
        require(request.getIsCorrect(), "isCorrect");
        require(request.getCourseId(), "courseId");
        require(request.getSubjectId(), "subjectId");
        require(request.getChapterId(), "chapterId");
        require(request.getTopicId(), "topicId");

        if (request.getUserId() != null
                && !request.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "userId does not match the logged-in user");
        }

        String eventType = request.getEventType() == null
                ? "ANSWER"
                : request.getEventType().trim().toUpperCase(Locale.ROOT);

        if (!EVENT_TYPES.contains(eventType)) {
            throw badRequest("eventType must be ANSWER, HINT or AUTOFILL");
        }

        // --- question and its hierarchy -----------------------------------
        Question question = findActiveQuestion(request.getQuestionId());

        verifyHierarchy(question, request);

        // --- unit ----------------------------------------------------------
        String requestedType = request.getQuestionType() == null
                ? ""
                : request.getQuestionType().trim().toUpperCase(Locale.ROOT);

        if (TYPE_MCQ.equals(requestedType) || isMcq(question)) {
            throw badRequest("MCQ results are recorded when the MCQ answer "
                    + "is submitted (POST /api/mcq-questions/submit); "
                    + "this endpoint is for attribute-based questions");
        }

        if (!TYPE_ATTRIBUTE.equals(requestedType)) {
            throw badRequest("questionType must be ATTRIBUTE");
        }

        if (isMatchingOrFillBlank(question)) {
            throw badRequest("Practice results for matching and "
                    + "fill-in-the-blank questions are not supported yet");
        }

        require(request.getQuestionAttributeId(), "questionAttributeId");

        QuestionAttribute questionAttribute = questionAttributeRepository
                .findById(request.getQuestionAttributeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Question attribute not found: "
                                + request.getQuestionAttributeId()));

        if (!question.getQuestionId()
                .equals(questionAttribute.getQuestion().getQuestionId())) {
            throw badRequest("questionAttributeId "
                    + request.getQuestionAttributeId()
                    + " does not belong to question "
                    + question.getQuestionId());
        }

        if (!Boolean.TRUE.equals(questionAttribute.getActiveRow())) {
            throw badRequest("Question attribute is inactive: "
                    + request.getQuestionAttributeId());
        }

        // --- 1. historical answer_events (existing mechanism) --------------
        AnswerEventRequestDTO eventRequest = new AnswerEventRequestDTO();
        eventRequest.setUserId(user.getUserId());
        eventRequest.setQuestionId(question.getQuestionId());
        // answer_events points at the accounting attribute
        // (table_attributes), not at the question_attributes row.
        eventRequest.setAttributeId(
                questionAttribute.getAttribute().getAttributeId());
        eventRequest.setAnswerPosition(
                request.getAnswerPosition() == null
                        ? 1
                        : request.getAnswerPosition());
        eventRequest.setArithmetic(request.getArithmetic());
        eventRequest.setEventType(eventType);
        eventRequest.setIsCorrect(request.getIsCorrect());
        eventRequest.setHint(request.getHint());
        eventRequest.setDescription(request.getDescription());
        eventRequest.setUserAnswer(request.getUserAnswer());

        AnswerEventResponseDTO savedEvent =
                answerEventService.createEvent(eventRequest);

        PracticeResultResponseDTO response = new PracticeResultResponseDTO();
        response.setAnswerEventId(savedEvent.getAnswerEventId());

        // An autofill is the app revealing the answer, not the student
        // attempting it - keep the event, don't count it as attempted.
        if ("AUTOFILL".equals(eventType)) {
            response.setResultRecorded(false);
            return response;
        }

        // --- 2. current practice_results ----------------------------------
        practiceResultRepository.upsertAttributeResult(
                user.getUserId(),
                question.getQuestionId(),
                questionAttribute.getQuestionAttributeId(),
                question.getCourse().getCourseId(),
                question.getSubject().getSubjectId(),
                question.getChapter().getChapterId(),
                question.getTopic().getTopicId(),
                request.getIsCorrect());

        PracticeResult saved = practiceResultRepository
                .findByUser_UserIdAndQuestion_QuestionIdAndQuestionAttribute_QuestionAttributeId(
                        user.getUserId(),
                        question.getQuestionId(),
                        questionAttribute.getQuestionAttributeId())
                .orElseThrow(() -> new IllegalStateException(
                        "Practice result was not saved"));

        return toResponse(response, saved);
    }


    /**
     * Called by the MCQ submit, which already saved its own answer_events row
     * and worked out correctness on the server. The whole question is the
     * unit, so there is no attribute. Joins the caller's transaction.
     */
    @Transactional
    public void recordMcqResult(
            User user,
            Question question,
            boolean isCorrect) {

        practiceResultRepository.upsertQuestionResult(
                user.getUserId(),
                question.getQuestionId(),
                question.getCourse().getCourseId(),
                question.getSubject().getSubjectId(),
                question.getChapter().getChapterId(),
                question.getTopic().getTopicId(),
                isCorrect);
    }


    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Question findActiveQuestion(Long questionId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Question not found: " + questionId));

        if (!Boolean.TRUE.equals(question.getActiveRow())) {
            throw badRequest("Question is inactive: " + questionId);
        }

        return question;
    }

    // The frontend sends the ids, but a question already knows where it
    // lives - so they're checked, never trusted.
    private void verifyHierarchy(
            Question question,
            PracticeResultRequestDTO request) {

        List<String> mismatches = new ArrayList<>();

        if (!Objects.equals(question.getCourse().getCourseId(),
                request.getCourseId())) {
            mismatches.add("courseId");
        }
        if (!Objects.equals(question.getSubject().getSubjectId(),
                request.getSubjectId())) {
            mismatches.add("subjectId");
        }
        if (!Objects.equals(question.getChapter().getChapterId(),
                request.getChapterId())) {
            mismatches.add("chapterId");
        }
        if (!Objects.equals(question.getTopic().getTopicId(),
                request.getTopicId())) {
            mismatches.add("topicId");
        }

        if (!mismatches.isEmpty()) {
            throw badRequest("Question " + question.getQuestionId()
                    + " does not belong to the given "
                    + String.join(", ", mismatches));
        }
    }

    // An MCQ has a mcq_questions row; that's what the MCQ flow itself uses.
    private boolean isMcq(Question question) {
        return mcqQuestionRepository.existsById(question.getQuestionId());
    }

    private boolean isMatchingOrFillBlank(Question question) {

        if (question.getQuestionType() == null
                || question.getQuestionType().getQuestionType() == null) {
            return false;
        }

        String name = question.getQuestionType().getQuestionType()
                .toUpperCase(Locale.ROOT);

        return name.contains("MATCH") || name.contains("FILL");
    }

    private PracticeResultResponseDTO toResponse(
            PracticeResultResponseDTO response,
            PracticeResult result) {

        response.setResultRecorded(true);
        response.setPracticeResultId(result.getPracticeResultId());
        response.setUserId(result.getUser().getUserId());
        response.setQuestionId(result.getQuestion().getQuestionId());
        response.setQuestionAttributeId(
                result.getQuestionAttribute() == null
                        ? null
                        : result.getQuestionAttribute().getQuestionAttributeId());
        response.setCourseId(result.getCourse().getCourseId());
        response.setSubjectId(result.getSubject().getSubjectId());
        response.setChapterId(result.getChapter().getChapterId());
        response.setTopicId(result.getTopic().getTopicId());
        response.setQuestionType(result.getQuestionType());
        response.setIsCorrect(result.getIsCorrect());
        response.setAttemptNumber(result.getAttemptNumber());
        response.setAnsweredAt(result.getAnsweredAt());

        return response;
    }

    private User getLoggedInUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        // Same rule as /api/answer_events: guests don't record practice
        // results.
        if (user.getRole() != null
                && "GUEST".equals(user.getRole().getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Guests cannot record practice results");
        }

        return user;
    }

    private void require(Object value, String field) {
        if (value == null) {
            throw badRequest(field + " is required");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
