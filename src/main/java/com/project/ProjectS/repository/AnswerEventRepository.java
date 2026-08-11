package com.project.ProjectS.repository;

import com.project.ProjectS.entity.AnswerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
@Repository
public interface AnswerEventRepository
        extends JpaRepository<AnswerEvent, Long> {

    List<AnswerEvent> findByQuestion_QuestionId(Long questionId);

    List<AnswerEvent> findByQuestion_QuestionIdAndActiveRowTrue(
            Long questionId
    );

    List<AnswerEvent> findByQuestion_QuestionIdAndValid(
            Long questionId,
            Boolean valid
    );

    @Modifying
    @Query("""
    UPDATE AnswerEvent e
    SET e.activeRow = false
    WHERE e.question.questionId = :questionId
      AND e.attribute.attributeId = :attributeId
      AND e.activeRow = true
""")
    int deactivateActiveAnswersByAttribute(
            @Param("questionId") Long questionId,
            @Param("attributeId") Long attributeId
    );
}