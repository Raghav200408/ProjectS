package com.project.ProjectS.service;


import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.QuestionAnswerRequestDTO;
import com.project.ProjectS.model.QuestionAnswerResponseDTO;
import com.project.ProjectS.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import com.project.ProjectS.repository.AnswerEventRepository;

import java.util.List;

@Service
@Transactional
public class QuestionAnswerService {
    private static final Logger log = LogManager.getLogger(QuestionAnswerService.class);

    @Autowired
    public QuestionAnswerService(QuestionAnswerRepository questionAnswerRepository, UserRepository userRepository, QuestionRepository questionRepository, TableNameRepository tableNameRepository, TableHeaderRepository tableHeaderRepository, TableAttributeRepository tableAttributeRepository) {
        this.questionAnswerRepository = questionAnswerRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.tableNameRepository = tableNameRepository;
        this.tableHeaderRepository = tableHeaderRepository;
        this.tableAttributeRepository = tableAttributeRepository;
    }

    private final QuestionAnswerRepository questionAnswerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TableNameRepository tableNameRepository;
    private final TableHeaderRepository tableHeaderRepository;
    private final TableAttributeRepository tableAttributeRepository;

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
        log.info("Saving answer via authenticated user: userId={} questionId={}",
                request != null ? request.getUserId() : null,
                request != null ? request.getQuestionId() : null);
        request.setUserId(getAuthenticatedUserId(authentication));
        return saveAnswer(request);
    }

    public QuestionAnswerResponseDTO saveAnswer(QuestionAnswerRequestDTO request) {
        log.info("Processing answer save request: userId={} questionId={} tableNameId={} headerId={} attributeId={} pairAttributeId={}",
                request.getUserId(), request.getQuestionId(), request.getTableNameId(),
                request.getHeaderId(), request.getAttributeId(), request.getPairAttributeId());
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

        log.info("Answer saved successfully: answerId={} userId={} questionId={}",
                savedAnswer.getAnswerId(), user.getUserId(), question.getQuestionId());

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

        return "Answers reset successfully";
    }

    private QuestionAnswerResponseDTO convertToResponse(
            QuestionAnswer answer) {

        QuestionAnswerResponseDTO response =
                new QuestionAnswerResponseDTO();


        response.setAnswerId(
                answer.getAnswerId()
        );


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
