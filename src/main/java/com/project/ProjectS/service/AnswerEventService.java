package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AnswerEventService {

    @Autowired
    private AnswerEventRepository answerEventRepository;

    @Autowired
    private RuleEngineRepository ruleEngineRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TableNameRepository tableNameRepository;

    @Autowired
    private TableHeaderRepository tableHeaderRepository;

    @Autowired
    private TableAttributeRepository tableAttributeRepository;


    public AnswerEventResponseDTO processAnswer(
            AnswerEventRequestDTO request) {

        //Find User
        User user = userRepository.findById(
                request.getUserId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "User not found with id: "
                                + request.getUserId()
                )
        );

        //Find Question
        Question question = questionRepository.findById(
                request.getQuestionId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Question not found with id: "
                                + request.getQuestionId()
                )
        );

        //Find Table
        TableName tableName = null;

        if (request.getTableNameId() != null) {

            tableName = tableNameRepository.findById(
                    request.getTableNameId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Table Name not found with id: "
                                    + request.getTableNameId()
                    )
            );
        }

        //Find Header
        TableHeader header = null;
        if (request.getHeaderId() != null) {
            header = tableHeaderRepository.findById(
                    request.getHeaderId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Table Header not found with id: "
                                    + request.getHeaderId()
                    )
            );
        }

        //Find Attribute
        TableAttribute attribute = null;

        if (request.getAttributeId() != null) {

            attribute = tableAttributeRepository.findById(
                    request.getAttributeId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Table Attribute not found with id: "
                                    + request.getAttributeId()
                    )
            );
        }

        //Validate answer against Rule Engine
        boolean valid = validateRuleEngine(
                request
        );

        //Create QuestionAnswer ONLY if correct
        QuestionAnswer savedAnswer = null;

        if (valid) {

            QuestionAnswer answer =
                    new QuestionAnswer();

            answer.setUser(user);

            answer.setQuestion(question);

            answer.setTableName(tableName);

            answer.setHeader(header);

            answer.setAttribute(attribute);

            answer.setArithmetic(
                    request.getArithmetic()
            );

            answer.setAmount(
                    request.getAmount()
            );

            answer.setActiveRow(true);

            answer.setRowStatus(1);

            savedAnswer =
                    questionAnswerRepository.save(answer);
        }

        //Create AnswerEvent for EVERY answer
        AnswerEvent event =
                new AnswerEvent();

        answerEventRepository.deactivateActiveAnswersByAttribute(
                request.getQuestionId(),
                request.getAttributeId()
        );

        event.setQuestion(question);
        event.setAnswer(savedAnswer);

        event.setTableName(tableName);
        event.setHeader(header);
        event.setAttribute(attribute);

        event.setArithmetic(
                request.getArithmetic()
        );

        event.setAmount(
                request.getAmount()
        );

        event.setDescription(
                request.getDescription()
        );

        event.setValid(valid);

        event.setAction(
                request.getAction()
        );

        event.setUserAnswer(
                request.getUserAnswer()
        );

        event.setAnswerBy(
                request.getAnswerBy()
        );

        event.setHint(
                request.getHint()
        );

        event.setActiveRow(true);


        AnswerEvent savedEvent =
                answerEventRepository.save(event);

        //Convert to response
        return convertToResponse(
                savedEvent
        );
    }
    // RULE ENGINE VALIDATION
    private boolean validateRuleEngine(
            AnswerEventRequestDTO request) {

        if (request.getAttributeId() == null) {
            return false;
        }

        List<RuleEngine> rules =
                ruleEngineRepository.findByAttributeId(
                        request.getAttributeId()
                );

        if (rules.isEmpty()) {
            return false;
        }


        for (RuleEngine rule : rules) {

            if (matches(
                    request,
                    rule.getTable1(),
                    rule.getHeader1())) {

                return true;
            }

            if (matches(
                    request,
                    rule.getTable2(),
                    rule.getHeader2())) {

                return true;
            }

            if (matches(
                    request,
                    rule.getTable3(),
                    rule.getHeader3())) {

                return true;
            }

            if (matches(
                    request,
                    rule.getTable4(),
                    rule.getHeader4())) {

                return true;
            }
        }

        return false;
    }
    // MATCH TABLE and HEADER

    private boolean matches(
            AnswerEventRequestDTO request,
            TableName table,
            TableHeader header) {

        if (table == null || header == null) {
            return false;
        }

        if (request.getTableNameId() == null
                || request.getHeaderId() == null) {

            return false;
        }

        return table.getTableNameId()
                .equals(request.getTableNameId())

                && header.getHeaderId()
                .equals(request.getHeaderId());
    }

    public List<AnswerEventResponseDTO> getEventsByQuestionId(
            Long questionId) {

        questionRepository.findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: "
                                        + questionId
                        )
                );

        return answerEventRepository
                .findByQuestion_QuestionIdAndActiveRowTrue(questionId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<AnswerEventResponseDTO> getMistakesByQuestionId(
            Long questionId) {

        questionRepository.findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: "
                                        + questionId
                        )
                );

        return answerEventRepository
                .findByQuestion_QuestionIdAndValid(
                        questionId,
                        false
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
    // RESPONSE MAPPER
    private AnswerEventResponseDTO convertToResponse(
            AnswerEvent event) {

        AnswerEventResponseDTO response =
                new AnswerEventResponseDTO();

        // Answer Event ID
        response.setAnswerEventId(
                event.getAnswerEventId()
        );

        // Question
        if (event.getQuestion() != null) {
            response.setQuestionId(
                    event.getQuestion().getQuestionId()
            );
        }

        // Answer ID
        if (event.getAnswer() != null) {
            response.setAnswerId(
                    event.getAnswer().getAnswerId()
            );
        }

        // Table
        if (event.getTableName() != null) {

            response.setTableNameId(
                    event.getTableName().getTableNameId()
            );

            response.setTableName(
                    event.getTableName().getName()
            );
        }

        // Header
        if (event.getHeader() != null) {

            response.setHeaderId(
                    event.getHeader().getHeaderId()
            );

            response.setHeaderName(
                    event.getHeader().getName()
            );
        }

        // Attribute
        if (event.getAttribute() != null) {

            response.setAttributeId(
                    event.getAttribute().getAttributeId()
            );

            response.setAttributeName(
                    event.getAttribute().getName()
            );
        }

        // Arithmetic
        response.setArithmetic(
                event.getArithmetic()
        );

        // Amount
        response.setAmount(
                event.getAmount()
        );

        // Valid
        response.setValid(
                event.getValid()
        );

        // Description
        response.setDescription(
                event.getDescription()
        );

        // Action
        response.setAction(
                event.getAction()
        );

        // User Answer
        response.setUserAnswer(
                event.getUserAnswer()
        );

        // Answer By
        response.setAnswerBy(
                event.getAnswerBy()
        );

        // Hint
        response.setHint(
                event.getHint()
        );

        // Active Row
        response.setActiveRow(
                event.getActiveRow()
        );

        // Created At
        response.setCreatedAt(
                event.getCreatedAt()
        );

        // Updated At
        response.setUpdatedAt(
                event.getUpdatedAt()
        );

        return response;
    }
}