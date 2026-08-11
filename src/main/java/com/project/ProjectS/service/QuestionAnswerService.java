package com.project.ProjectS.service;


import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.QuestionAnswerRequestDTO;
import com.project.ProjectS.model.QuestionAnswerResponseDTO;
import com.project.ProjectS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.ProjectS.repository.AnswerEventRepository;

import java.util.List;

@Service
@Transactional
public class QuestionAnswerService {
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


        QuestionAnswer answer = new QuestionAnswer();

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
    public String resetAnswersByQuestionId(Long questionId) {

        int answerCount =
                questionAnswerRepository
                        .deactivateByQuestionId(questionId);

        return answerCount +
                " answer(s) reset successfully.";
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

