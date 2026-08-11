package com.project.ProjectS.repository;

import com.project.ProjectS.entity.AnswerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerEventRepository
        extends JpaRepository<AnswerEvent, Long> {


    // Count previous ANSWER attempts
    long countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventType(
            Long userId,
            Long questionId,
            Long attributeId,
            String eventType
    );


    // Check whether HINT/AUTO_FILL was used
    boolean existsByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventType(
            Long userId,
            Long questionId,
            Long attributeId,
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
}