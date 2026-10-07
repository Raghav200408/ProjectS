package com.project.ProjectS.service;


import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.QuestionAnswerRequestDTO;
import com.project.ProjectS.model.QuestionAnswerResponseDTO;
import com.project.ProjectS.model.RuleConditionDTO;
import com.project.ProjectS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import com.project.ProjectS.repository.AnswerEventRepository;

import java.util.List;

@Service
@Transactional
public class QuestionAnswerService {
    @Autowired
    public QuestionAnswerService(QuestionAnswerRepository questionAnswerRepository, UserRepository userRepository, QuestionRepository questionRepository, TableNameRepository tableNameRepository, TableHeaderRepository tableHeaderRepository, TableAttributeRepository tableAttributeRepository,
            QuestionAttributeRepository questionAttributeRepository, RuleEngineService ruleEngineService,
            AnswerEventRepository answerEventRepository) {
        this.questionAnswerRepository = questionAnswerRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.tableNameRepository = tableNameRepository;
        this.tableHeaderRepository = tableHeaderRepository;
        this.tableAttributeRepository = tableAttributeRepository;
        this.questionAttributeRepository = questionAttributeRepository;
        this.ruleEngineService = ruleEngineService;
        this.answerEventRepository = answerEventRepository;
    }

    public QuestionAnswerService(QuestionAnswerRepository questionAnswerRepository, UserRepository userRepository,
            QuestionRepository questionRepository, TableNameRepository tableNameRepository,
            TableHeaderRepository tableHeaderRepository, TableAttributeRepository tableAttributeRepository,
            QuestionAttributeRepository questionAttributeRepository, RuleEngineService ruleEngineService) {
        this(questionAnswerRepository, userRepository, questionRepository, tableNameRepository,
                tableHeaderRepository, tableAttributeRepository, questionAttributeRepository, ruleEngineService, null);
    }

    public QuestionAnswerService(QuestionAnswerRepository questionAnswerRepository, UserRepository userRepository,
            QuestionRepository questionRepository, TableNameRepository tableNameRepository,
            TableHeaderRepository tableHeaderRepository, TableAttributeRepository tableAttributeRepository) {
        this(questionAnswerRepository, userRepository, questionRepository, tableNameRepository,
                tableHeaderRepository, tableAttributeRepository, null, null);
    }

    private final QuestionAnswerRepository questionAnswerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TableNameRepository tableNameRepository;
    private final TableHeaderRepository tableHeaderRepository;
    private final TableAttributeRepository tableAttributeRepository;
    private final QuestionAttributeRepository questionAttributeRepository;
    private final RuleEngineService ruleEngineService;
    private final AnswerEventRepository answerEventRepository;

    public Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authenticated user is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getUserId();
    }

    public QuestionAnswerResponseDTO saveAnswer(
            QuestionAnswerRequestDTO request,
            Authentication authentication) {
        request.setUserId(getAuthenticatedUserId(authentication));
        return saveAnswer(request);
    }

    public QuestionAnswerResponseDTO saveAnswer(QuestionAnswerRequestDTO request) {
        User user = userRepository.findById(
                request.getUserId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "User not found with id: "
                                + request.getUserId()
                )
        );
        Question question = questionRepository.findById(request.getQuestionId()).orElseThrow(() ->
                new RuntimeException(
                        "Question not found with id: "
                                + request.getQuestionId()
                )
        );
        if (RuleEngineService.isFinalAccountsDragDrop(question)) {
            user = lockFinalAccountsUser(request.getUserId());
            QuestionAttribute row = questionAttributeRepository.findByQuestion_QuestionId(request.getQuestionId())
                    .stream().filter(qa -> request.getQuestionAttributeId() != null
                            && request.getQuestionAttributeId().equals(qa.getQuestionAttributeId())
                            && qa.getAttribute() != null
                            && qa.getAttribute().getAttributeId().equals(request.getAttributeId())
                            && !Boolean.FALSE.equals(qa.getActiveRow()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("A valid question row is required"));
            if (!ruleEngineService.validatesFinalAccountsPlacement(question, row, request.getTableNameId(),
                    request.getHeaderId(), request.getArithmetic(), request.getAmount(), request.getConditionId())) {
                throw new IllegalArgumentException("The placement or amount does not match this Final Accounts row");
            }
            // Saving is per effect. Treat a repeated request as a retry so a restored proforma cannot double-count it.
            QuestionAnswer existing = questionAnswerRepository
                    .findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(request.getUserId(), request.getQuestionId())
                    .stream().filter(answer -> request.getQuestionAttributeId().equals(answer.getQuestionAttributeId())
                            && java.util.Objects.equals(request.getConditionId(), answer.getConditionId())
                            && answer.getTableName() != null && answer.getHeader() != null
                            && request.getTableNameId().equals(answer.getTableName().getTableNameId())
                            && request.getHeaderId().equals(answer.getHeader().getHeaderId())
                            && answer.getArithmetic() != null
                            && request.getArithmetic().trim().equalsIgnoreCase(answer.getArithmetic().trim())
                            && answer.getAmount() != null && request.getAmount().compareTo(answer.getAmount()) == 0)
                    .findFirst().orElse(null);
            if (existing != null) {
                return convertToResponse(existing);
            }
        }
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

        TableAttribute pairAttribute = null;

        if (request.getPairAttributeId() != null) {

            pairAttribute = tableAttributeRepository.findById(
                    request.getPairAttributeId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Table Attribute not found with id: "
                                    + request.getAttributeId()
                    )
            );
        }


        QuestionAnswer answer = new QuestionAnswer();

        answer.setUser(user);

        answer.setQuestion(question);

        answer.setTableName(tableName);

        answer.setHeader(header);

        answer.setAttribute(attribute);
        answer.setQuestionAttributeId(request.getQuestionAttributeId());

        answer.setPairAttribute(pairAttribute);

        answer.setConditionId(request.getConditionId());

        answer.setTotalAnswers(request.getTotalAnswers());

        answer.setArithmetic(
                request.getArithmetic()
        );

        answer.setAmount(
                request.getAmount()
        );

        answer.setActiveRow(true);

        answer.setRowStatus(1);


        QuestionAnswer savedAnswer =
                questionAnswerRepository.save(answer);


        return convertToResponse(savedAnswer);
    }

    public List<QuestionAnswerResponseDTO>
    getAnswersByQuestionId(Long questionId) {

        // Verify question exists
        questionRepository.findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: "
                                        + questionId
                        )
                );


        return questionAnswerRepository
                .findByQuestion_QuestionIdAndActiveRowTrue(
                        questionId
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<QuestionAnswerResponseDTO>
    getAnswersByUserAndQuestion(
            Long userId,
            Long questionId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        questionRepository.findById(questionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found with id: " + questionId
                        )
                );

        return questionAnswerRepository
                .findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(
                        userId,
                        questionId
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public String resetAnswersByUserAndQuestion(
            Long userId,
            Long questionId) {

        Question question = questionRepository.findById(questionId).orElse(null);
        if (RuleEngineService.isFinalAccountsDragDrop(question)) {
            lockFinalAccountsUser(userId);
        }

        List<QuestionAnswer> answers =
                questionAnswerRepository
                        .findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(
                                userId,
                                questionId
                        );

        for (QuestionAnswer answer : answers) {
            answer.setActiveRow(false);
        }

        questionAnswerRepository.saveAll(answers);

        // A fresh Final Accounts attempt must not inherit an AUTOFILL lock
        // from the answers just reset. Both updates use this transaction,
        // including when autofill succeeded but no placement was saved.
        if (RuleEngineService.isFinalAccountsDragDrop(question)) {
            answerEventRepository.deactivatePracticeAutofill(userId, questionId);
        }

        return "Answers reset successfully";
    }

    public User lockFinalAccountsUser(Long userId) {
        return userRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public boolean hasFinalAccountsPlacement(Long userId, Question question, QuestionAttribute row,
            RuleConditionDTO condition) {
        long occurrences = questionAttributeRepository.findByQuestion_QuestionId(question.getQuestionId()).stream()
                .filter(qa -> qa.getAttribute() != null && !Boolean.FALSE.equals(qa.getActiveRow())
                        && qa.getAttribute().getAttributeId().equals(row.getAttribute().getAttributeId()))
                .count();
        return questionAnswerRepository.findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(userId, question.getQuestionId())
                .stream().anyMatch(answer -> answer.getAttribute() != null
                        && answer.getAttribute().getAttributeId().equals(row.getAttribute().getAttributeId())
                        && (row.getQuestionAttributeId().equals(answer.getQuestionAttributeId())
                            || (answer.getQuestionAttributeId() == null && occurrences == 1))
                        && answer.getTableName() != null && answer.getHeader() != null
                        && RuleEngineService.matchesPlacement(condition, row, answer.getTableName().getTableNameId(),
                                answer.getHeader().getHeaderId(), answer.getArithmetic(), answer.getAmount()));
    }

    private QuestionAnswerResponseDTO convertToResponse(
            QuestionAnswer answer) {

        QuestionAnswerResponseDTO response =
                new QuestionAnswerResponseDTO();


        response.setAnswerId(
                answer.getAnswerId()
        );
        response.setQuestionAttributeId(answer.getQuestionAttributeId());


        if (answer.getUser() != null) {

            response.setUserId(
                    answer.getUser().getUserId()
            );
        }


        if (answer.getQuestion() != null) {

            response.setQuestionId(
                    answer.getQuestion().getQuestionId()
            );
        }


        if (answer.getTableName() != null) {

            response.setTableNameId(
                    answer.getTableName().getTableNameId()
            );

            response.setTableName(
                    answer.getTableName().getName()
            );
        }


        if (answer.getHeader() != null) {

            response.setHeaderId(
                    answer.getHeader().getHeaderId()
            );

            response.setHeaderName(
                    answer.getHeader().getName()
            );
        }


        if (answer.getAttribute() != null) {

            response.setAttributeId(
                    answer.getAttribute().getAttributeId()
            );

            response.setAttributeName(
                    answer.getAttribute().getName()
            );
        }

        if (answer.getPairAttribute() != null) {

            response.setPairAttributeId(
                    answer.getPairAttribute().getAttributeId()
            );

            response.setPairAttributeName(
                    answer.getPairAttribute().getName()
            );
        }

        if (answer.getConditionId() != null) {

            response.setConditionId(
                    answer.getConditionId()
            );
        }

        if (answer.getTotalAnswers() != null) {

            response.setTotalAnswers(
                    answer.getTotalAnswers()
            );
        }


        response.setArithmetic(
                answer.getArithmetic()
        );

        response.setAmount(
                answer.getAmount()
        );

        response.setActiveRow(
                answer.getActiveRow()
        );

        response.setCreatedAt(
                answer.getCreatedAt()
        );

        response.setRowStatus(
                answer.getRowStatus()
        );

        response.setUpdatedAt(
                answer.getUpdatedAt()
        );


        return response;
    }


}
