package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class AnswerEventService {

    @Autowired
    private AnswerEventRepository answerEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TableAttributeRepository tableAttributeRepository;




    // =========================================================
    // CREATE EVENT
    // =========================================================

    public AnswerEventResponseDTO createEvent(
            AnswerEventRequestDTO request) {

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: "
                                        + request.getUserId()
                        )
                );


        Question question = questionRepository
                .findById(request.getQuestionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found: "
                                        + request.getQuestionId()
                        )
                );


        TableAttribute attribute =
                tableAttributeRepository
                        .findById(request.getAttributeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attribute not found: "
                                                + request.getAttributeId()
                                )
                        );





        String eventType =
                request.getEventType()
                        .trim()
                        .toUpperCase();


        // =====================================================
        // ATTEMPT NUMBER
        // =====================================================

        int attemptNumber = 0;

        if ("ANSWER".equals(eventType)) {

            long previousAttempts =
                    answerEventRepository
                            .countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventType(
                                    request.getUserId(),
                                    request.getQuestionId(),
                                    request.getAttributeId(),
                                    "ANSWER"
                            );

            attemptNumber = (int) previousAttempts + 1;
        }


        // =====================================================
        // CHECK HINT / AUTO-FILL HISTORY
        // =====================================================

        boolean assistanceUsed = false;

        if ("ANSWER".equals(eventType)) {

            boolean hintUsed =
                    answerEventRepository
                            .existsByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventType(
                                    request.getUserId(),
                                    request.getQuestionId(),
                                    request.getAttributeId(),
                                    "HINT"
                            );

            boolean autoFillUsed =
                    answerEventRepository
                            .existsByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventType(
                                    request.getUserId(),
                                    request.getQuestionId(),
                                    request.getAttributeId(),
                                    "AUTO_FILL"
                            );

            assistanceUsed = hintUsed || autoFillUsed;
        }


        // =====================================================
        // CALCULATE MARKS
        // =====================================================

        BigDecimal marks = calculateMarks(
                eventType,
                attemptNumber,
                request.getIsCorrect(),
                assistanceUsed
        );


        // =====================================================
        // CREATE ENTITY
        // =====================================================

        AnswerEvent event = new AnswerEvent();

        event.setUser(user);
        event.setQuestion(question);
        event.setAttribute(attribute);


        event.setArithmetic(request.getArithmetic());

        event.setEventType(eventType);

        event.setIsCorrect(request.getIsCorrect());

        event.setAttemptNumber(attemptNumber);

        event.setMarks(marks);

        event.setHint(request.getHint());

        event.setDescription(request.getDescription());

        event.setUserAnswer(request.getUserAnswer());

        event.setActiveRow(true);


        AnswerEvent saved =
                answerEventRepository.save(event);


        return convertToResponse(saved);
    }


    // =========================================================
    // MARK CALCULATION
    // =========================================================

    private BigDecimal calculateMarks(
            String eventType,
            int attemptNumber,
            Boolean isCorrect,
            boolean assistanceUsed) {


        // HINT
        if ("HINT".equals(eventType)) {
            return BigDecimal.ZERO;
        }


        // AUTO FILL
        if ("AUTO_FILL".equals(eventType)) {
            return BigDecimal.ZERO;
        }


        // Only ANSWER gets marks
        if (!"ANSWER".equals(eventType)) {
            return BigDecimal.ZERO;
        }


        // Wrong answer
        if (!Boolean.TRUE.equals(isCorrect)) {
            return BigDecimal.ZERO;
        }


        // Hint or Auto-fill was already used
        if (assistanceUsed) {
            return BigDecimal.ZERO;
        }


        // First correct attempt
        if (attemptNumber == 1) {
            return new BigDecimal("1.00");
        }


        // Second correct attempt
        if (attemptNumber == 2) {
            return new BigDecimal("0.50");
        }


        // Third or later
        return BigDecimal.ZERO;
    }



    public List<AnswerEventResponseDTO> getAllEvents() {

        return answerEventRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    public AnswerEventResponseDTO getById(
            Long answerEventId) {

        AnswerEvent event =
                answerEventRepository
                        .findById(answerEventId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Answer event not found: "
                                                + answerEventId
                                )
                        );

        return convertToResponse(event);
    }

    public List<AnswerEventResponseDTO>
    getByUserQuestionAttribute(
            Long userId,
            Long questionId,
            Long attributeId) {

        return answerEventRepository
                .findByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeId(
                        userId,
                        questionId,
                        attributeId
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
    public List<AnswerEventResponseDTO> getAllMistakesByUser(
            Long userId) {

        return answerEventRepository
                .findByUser_UserIdAndEventTypeAndIsCorrectFalse(
                        userId,
                        "ANSWER"
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<AnswerEventResponseDTO>
    getMistakes(
            Long userId,
            Long questionId) {

        return answerEventRepository
                .findByUser_UserIdAndQuestion_QuestionIdAndEventTypeAndIsCorrectFalse(
                        userId,
                        questionId,
                        "ANSWER"
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
    public BigDecimal getOverallMarks(Long userId) {

        List<AnswerEvent> events =
                answerEventRepository.findByUser_UserId(userId);

        return events.stream()
                .map(AnswerEvent::getMarks)
                .filter(Objects::nonNull)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
    private AnswerEventResponseDTO convertToResponse(
            AnswerEvent event) {

        AnswerEventResponseDTO response =
                new AnswerEventResponseDTO();


        response.setAnswerEventId(
                event.getAnswerEventId()
        );


        if (event.getUser() != null) {

            response.setUserId(
                    event.getUser().getUserId()
            );

            response.setUsername(
                    event.getUser().getName()
            );
        }


        if (event.getQuestion() != null) {
            response.setQuestionId(
                    event.getQuestion().getQuestionId()
            );
        }


        if (event.getAttribute() != null) {

            response.setAttributeId(
                    event.getAttribute().getAttributeId()
            );

            response.setAttributeName(
                    event.getAttribute().getName()
            );
        }








        response.setArithmetic(
                event.getArithmetic()
        );

        response.setEventType(
                event.getEventType()
        );

        response.setIsCorrect(
                event.getIsCorrect()
        );

        response.setAttemptNumber(
                event.getAttemptNumber()
        );

        response.setMarks(
                event.getMarks()
        );

        response.setHint(
                event.getHint()
        );

        response.setDescription(
                event.getDescription()
        );

        response.setUserAnswer(
                event.getUserAnswer()
        );

        response.setActiveRow(
                event.getActiveRow()
        );

        response.setCreatedAt(
                event.getCreatedAt()
        );

        response.setUpdatedAt(
                event.getUpdatedAt()
        );

        return response;
    }
}