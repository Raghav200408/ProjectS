package com.project.ProjectS.service;

import com.project.ProjectS.entity.AnswerEvent;
import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.repository.AnswerEventRepository;
import com.project.ProjectS.repository.QuestionRepository;
import com.project.ProjectS.repository.TableAttributeRepository;
import com.project.ProjectS.repository.UserRepository;
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
    public AnswerEventService(AnswerEventRepository answerEventRepository, UserRepository userRepository, QuestionRepository questionRepository, TableAttributeRepository tableAttributeRepository) {
        this.answerEventRepository = answerEventRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.tableAttributeRepository = tableAttributeRepository;
    }

    private final AnswerEventRepository answerEventRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TableAttributeRepository tableAttributeRepository;


    public AnswerEventResponseDTO createEvent(
            AnswerEventRequestDTO request) {

        String eventType = request.getEventType()
                .trim()
                .toUpperCase();

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + request.getUserId()));


        Question question = questionRepository
                .findById(request.getQuestionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found: " + request.getQuestionId()));


        TableAttribute attribute =
                tableAttributeRepository
                        .findById(request.getAttributeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attribute not found: " + request.getAttributeId()));


        int attemptNumber = 0;
        BigDecimal marks = BigDecimal.ZERO;


        switch (eventType) {


            case "ANSWER": {

                boolean autoFillUsed =
                        answerEventRepository
                                .existsByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                        request.getUserId(),
                                        request.getQuestionId(),
                                        request.getAttributeId(),
                                        request.getAnswerPosition(),
                                        "AUTOFILL"
                                );

                if (autoFillUsed) {
                    throw new IllegalStateException(
                            "Answer already autofilled for answer position "
                                    + request.getAnswerPosition()
                    );
                }

                long previousAttempts =
                        answerEventRepository
                                .countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                        request.getUserId(),
                                        request.getQuestionId(),
                                        request.getAttributeId(),
                                        request.getAnswerPosition(),
                                        "ANSWER"
                                );

                attemptNumber = (int) previousAttempts + 1;

                if (Boolean.TRUE.equals(request.getIsCorrect())) {
                    marks = calculateAnswerMarks(attemptNumber);
                } else {
                    marks = BigDecimal.ZERO;
                }

                break;
            }

            case "HINT": {

                long previousAttempts =
                        answerEventRepository
                                .countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                        request.getUserId(),
                                        request.getQuestionId(),
                                        request.getAttributeId(),
                                        request.getAnswerPosition(),
                                        "ANSWER"
                                );

                attemptNumber = (int) previousAttempts + 1;

                // Correct or wrong after hint = 0 marks
                marks = BigDecimal.ZERO;

                break;
            }

            case "AUTOFILL": {

                attemptNumber = 0;
                marks = BigDecimal.ZERO;

                break;
            }

            default:

                throw new IllegalArgumentException(
                        "Invalid event type: "
                                + request.getEventType()
                );
        }


        AnswerEvent event = new AnswerEvent();

        event.setUser(user);

        event.setQuestion(question);

        event.setAttribute(attribute);


        event.setAnswerPosition(
                request.getAnswerPosition()
        );


        event.setArithmetic(
                request.getArithmetic()
        );


        event.setEventType(
                eventType
        );


        event.setIsCorrect(
                request.getIsCorrect()
        );


        event.setAttemptNumber(
                attemptNumber
        );


        event.setMarks(
                marks
        );


        event.setHint(
                request.getHint()
        );


        event.setDescription(
                request.getDescription()
        );


        event.setUserAnswer(
                request.getUserAnswer()
        );


        event.setActiveRow(true);


        AnswerEvent saved =
                answerEventRepository.save(event);


        return convertToResponse(saved);
    }


    private BigDecimal calculateAnswerMarks(
            int attemptNumber) {

        return switch (attemptNumber) {

            case 1 -> new BigDecimal("1.00");

            case 2 -> new BigDecimal("0.50");

            default -> BigDecimal.ZERO;
        };
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


    public int resetEvents(
            Long userId,
            Long questionId) {

        return answerEventRepository
                .deactivateByUserAndQuestion(
                        userId,
                        questionId
                );
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


    public List<AnswerEventResponseDTO>
    getAllMistakesByUser(Long userId) {

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


    public BigDecimal getOverallMarks(
            Long userId) {

        List<AnswerEvent> events =
                answerEventRepository
                        .findByUser_UserId(userId);

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


        response.setAnswerPosition(
                event.getAnswerPosition()
        );


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
