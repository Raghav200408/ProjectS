package com.project.ProjectS.repository;

import com.project.ProjectS.entity.AnswerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerEventRepository
        extends JpaRepository<AnswerEvent, Long> {


    long countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
            Long userId,
            Long questionId,
            Long attributeId,
            Integer answerPosition,
            String eventType
    );

    boolean existsByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
            Long userId,
            Long questionId,
            Long attributeId,
            Integer answerPosition,
            String eventType
    );


    // Get all events for attribute
    List<AnswerEvent>
    findByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeId(
            Long userId,
            Long questionId,
            Long attributeId
    );


    // Get wrong ANSWER events
    List<AnswerEvent>
    findByUser_UserIdAndQuestion_QuestionIdAndEventTypeAndIsCorrectFalse(
            Long userId,
            Long questionId,
            String eventType
    );

    List<AnswerEvent>
    findByUser_UserIdAndEventTypeAndIsCorrectFalse(
            Long userId,
            String eventType
    );

    List<AnswerEvent> findByUser_UserId(Long userId);

    @Modifying
    @Query("""
                UPDATE AnswerEvent ae
                SET ae.activeRow = false
                WHERE ae.user.userId = :userId
                  AND ae.question.questionId = :questionId
                  AND ae.activeRow = true
            """)
    int deactivateByUserAndQuestion(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId
    );


}