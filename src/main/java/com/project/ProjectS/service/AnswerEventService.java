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
    public AnswerEventService(
            AnswerEventRepository answerEventRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository,
            TableAttributeRepository tableAttributeRepository,
            QuestionFillBlankAnswerRepository fillBlankAnswerRepository) {

        this.answerEventRepository = answerEventRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.tableAttributeRepository = tableAttributeRepository;
        this.fillBlankAnswerRepository = fillBlankAnswerRepository;
    }

    private final AnswerEventRepository answerEventRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TableAttributeRepository tableAttributeRepository;
    private final QuestionFillBlankAnswerRepository fillBlankAnswerRepository;


    public AnswerEventResponseDTO createEvent(
            AnswerEventRequestDTO request) {

        String eventType = request.getEventType()
                .trim()
                .toUpperCase();

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


        /*
         * Attribute is optional for Fill in the Blank.
         * Existing question types can still provide attributeId.
         */
        TableAttribute attribute = null;

        if (request.getAttributeId() != null) {

            attribute = tableAttributeRepository
                    .findById(request.getAttributeId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Attribute not found: "
                                            + request.getAttributeId()
                            )
                    );
        }


        int attemptNumber = 0;

        BigDecimal marks = BigDecimal.ZERO;


        /*
         * Check whether this is a Fill in the Blank question.
         *
         * Supports both:
         * FILL_IN_THE_BLANK
         * FILL_IN_THE_BLANKS
         */
        boolean isFillInTheBlank =
                question.getQuestionType() != null
                        && (
                        "FILL_IN_THE_BLANK".equalsIgnoreCase(
                                question.getQuestionType().getQuestionType()
                        )
                                || "FILL_IN_THE_BLANKS".equalsIgnoreCase(
                                question.getQuestionType().getQuestionType()
                        )
                );


        /*
         * This will contain the final correctness result.
         *
         * For Fill in the Blank it will be calculated below.
         *
         * For all other question types your existing
         * request.getIsCorrect() value is preserved.
         */
        Boolean finalIsCorrect = request.getIsCorrect();


        switch (eventType) {


            case "ANSWER": {

                boolean autoFillUsed;


                /*
                 * Fill in the Blank:
                 *
                 * userId + questionId + answerPosition
                 *
                 * Attribute is not required.
                 */
                if (isFillInTheBlank) {

                    autoFillUsed =
                            answerEventRepository
                                    .existsByUser_UserIdAndQuestion_QuestionIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                            request.getUserId(),
                                            request.getQuestionId(),
                                            request.getAnswerPosition(),
                                            "AUTOFILL"
                                    );

                } else {

                    /*
                     * Existing question types:
                     *
                     * userId + questionId + attributeId + answerPosition
                     *
                     * Existing behavior is preserved.
                     */
                    autoFillUsed =
                            answerEventRepository
                                    .existsByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                            request.getUserId(),
                                            request.getQuestionId(),
                                            request.getAttributeId(),
                                            request.getAnswerPosition(),
                                            "AUTOFILL"
                                    );
                }


                if (autoFillUsed) {

                    throw new IllegalStateException(
                            "Answer already autofilled for answer position "
                                    + request.getAnswerPosition()
                    );
                }


                long previousAttempts;


                /*
                 * Fill in the Blank attempt count.
                 */
                if (isFillInTheBlank) {

                    previousAttempts =
                            answerEventRepository
                                    .countByUser_UserIdAndQuestion_QuestionIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                            request.getUserId(),
                                            request.getQuestionId(),
                                            request.getAnswerPosition(),
                                            "ANSWER"
                                    );

                } else {

                    /*
                     * Existing attempt count logic.
                     */
                    previousAttempts =
                            answerEventRepository
                                    .countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                            request.getUserId(),
                                            request.getQuestionId(),
                                            request.getAttributeId(),
                                            request.getAnswerPosition(),
                                            "ANSWER"
                                    );
                }


                attemptNumber = (int) previousAttempts + 1;


                /*
                 * =====================================================
                 * ONLY NEW FILL-IN-THE-BLANK CODE
                 * =====================================================
                 *
                 * Do not trust the frontend isCorrect value.
                 *
                 * Check the student's answer against ALL accepted
                 * answers stored for this question + blank number.
                 */
                if (isFillInTheBlank) {

                    finalIsCorrect =
                            evaluateFillInTheBlankAnswer(
                                    request.getQuestionId(),
                                    request.getAnswerPosition(),
                                    request.getUserAnswer()
                            );
                }


                /*
                 * Existing marks logic.
                 *
                 * For normal question types this uses the original
                 * request.getIsCorrect() value.
                 *
                 * For Fill in the Blank it uses the calculated
                 * finalIsCorrect value.
                 */
                if (Boolean.TRUE.equals(finalIsCorrect)) {

                    marks = calculateAnswerMarks(attemptNumber);

                } else {

                    marks = BigDecimal.ZERO;
                }


                break;
            }


            case "HINT": {

                long previousAttempts;


                /*
                 * Fill in the Blank hint attempt count.
                 */
                if (isFillInTheBlank) {

                    previousAttempts =
                            answerEventRepository
                                    .countByUser_UserIdAndQuestion_QuestionIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                            request.getUserId(),
                                            request.getQuestionId(),
                                            request.getAnswerPosition(),
                                            "ANSWER"
                                    );

                } else {

                    /*
                     * Existing question types.
                     */
                    previousAttempts =
                            answerEventRepository
                                    .countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                                            request.getUserId(),
                                            request.getQuestionId(),
                                            request.getAttributeId(),
                                            request.getAnswerPosition(),
                                            "ANSWER"
                                    );
                }


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


        /*
         * For Fill in the Blank:
         *     finalIsCorrect = backend calculated result.
         *
         * For all other question types:
         *     finalIsCorrect = request.getIsCorrect()
         *
         * Therefore existing behavior is preserved.
         */
        event.setIsCorrect(
                finalIsCorrect
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


    /*
     * =========================================================
     * NEW METHOD
     * =========================================================
     *
     * Checks the student's Fill in the Blank answer against
     * every accepted answer for the specified blank.
     */
    private boolean evaluateFillInTheBlankAnswer(
            Long questionId,
            Integer answerPosition,
            String userAnswer) {

        /*
         * Empty answer = incorrect.
         */
        if (userAnswer == null ||
                userAnswer.trim().isEmpty()) {

            return false;
        }


        /*
         * Answer position represents the blank number.
         *
         * Example:
         *
         * blank 1 -> answerPosition 1
         * blank 2 -> answerPosition 2
         */
        if (answerPosition == null ||
                answerPosition <= 0) {

            return false;
        }


        /*
         * Get ALL correct answers for this particular
         * question and blank.
         *
         * Example for question 37:
         *
         * CPU
         * Central Processing Unit
         * central processing unit
         */
        List<QuestionFillBlankAnswer> acceptedAnswers =
                fillBlankAnswerRepository
                        .findByQuestionQuestionIdAndBlankNumberAndIsCorrectTrueOrderByDisplayOrderAsc(
                                questionId,
                                answerPosition
                        );


        /*
         * No accepted answers configured.
         */
        if (acceptedAnswers == null ||
                acceptedAnswers.isEmpty()) {

            return false;
        }


        String normalizedUserAnswer =
                userAnswer.trim();


        /*
         * Check against ALL accepted answers.
         *
         * trim()
         *      removes leading/trailing spaces.
         *
         * equalsIgnoreCase()
         *      allows CPU, cpu, Cpu, etc.
         *
         * anyMatch()
         *      means any one accepted answer is enough.
         */
        return acceptedAnswers.stream()
                .anyMatch(answer -> {

                    if (answer == null ||
                            answer.getAnswerText() == null) {

                        return false;
                    }

                    return answer.getAnswerText()
                            .trim()
                            .equalsIgnoreCase(
                                    normalizedUserAnswer
                            );
                });
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

        // Reset only the current answer cycle. Practice performance persists
        // across resets and is updated when the student answers again.
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

        // Practice-only: exam and mock-exam attempts are scored and recorded
        // separately (ExamResult / MockExamResult) and must never be blended
        // into this total.
        List<AnswerEvent> events =
                answerEventRepository
                        .findByUser_UserIdAndExamIsNullAndMockExamIsNull(userId);

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
            response.setQuestionText(
                    event.getQuestion().getQuestionText()
            );

            if (event.getQuestion().getChapter() != null) {

                response.setChapterId(
                        event.getQuestion()
                                .getChapter()
                                .getChapterId()
                );

                response.setChapterName(
                        event.getQuestion()
                                .getChapter()
                                .getName()
                );
            }

            if (event.getQuestion().getTopic() != null) {

                response.setTopicId(
                        event.getQuestion()
                                .getTopic()
                                .getTopicId()
                );

                response.setTopicName(
                        event.getQuestion()
                                .getTopic()
                                .getName()
                );
            }
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
