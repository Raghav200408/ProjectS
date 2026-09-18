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

    // MCQ attempt count
    long countByUser_UserIdAndQuestion_QuestionIdAndEventTypeAndActiveRowTrue(
            Long userId,
            Long questionId,
            String eventType
    );

    // Existing attribute-based attempt count
    long countByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
            Long userId,
            Long questionId,
            Long attributeId,
            Integer answerPosition,
            String eventType
    );

    // Fill in the Blank attempt count
    long countByUser_UserIdAndQuestion_QuestionIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
            Long userId,
            Long questionId,
            Integer answerPosition,
            String eventType
    );

    // Fill in the Blank autofill check
    boolean existsByUser_UserIdAndQuestion_QuestionIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
            Long userId,
            Long questionId,
            Integer answerPosition,
            String eventType
    );

    // Existing attribute-based autofill check
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

    // Practice-only events, for the practice "Total Score" widget - exam and
    // mock-exam attempts must never be blended into this.
    List<AnswerEvent> findByUser_UserIdAndExamIsNullAndMockExamIsNull(Long userId);

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

    // Journal/Dropdown/Drag-and-drop/MCQ's own "what did the student do" rows
    // for one exam attempt (Exam Review screen).
    List<AnswerEvent> findByUser_UserIdAndExam_ExamIdAndEventType(
            Long userId,
            Long examId,
            String eventType
    );

    // Same, for one mock-exam attempt.
    List<AnswerEvent> findByUser_UserIdAndMockExam_MockExamIdAndEventType(
            Long userId,
            Long mockExamId,
            String eventType
    );

    // Used before persistAnswerInfo writes a fresh exam submission's rows, so
    // retaking the same exam wipes every row from the previous attempt in one
    // shot instead of leaving them to pile up alongside the new ones. Scoped
    // to one event type so it never touches the practice flow's own
    // ANSWER/HINT events. Derived deleteBy queries - no @Modifying needed,
    // that's only for @Query methods.
    long deleteByUser_UserIdAndExam_ExamIdAndEventType(
            Long userId,
            Long examId,
            String eventType
    );

    // Same, for retaking a mock exam.
    long deleteByUser_UserIdAndMockExam_MockExamIdAndEventType(
            Long userId,
            Long mockExamId,
            String eventType
    );

    // The wrong lines this student submitted for one attribute on one exam
    // attempt - what the "What went wrong?" panel on the review screen shows
    // when a trial-balance / transaction row is clicked.
    List<AnswerEvent> findByUser_UserIdAndExam_ExamIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventTypeAndIsCorrectFalse(
            Long userId,
            Long examId,
            Long questionId,
            Long attributeId,
            String eventType
    );

    // Same, for one mock-exam attempt.
    List<AnswerEvent> findByUser_UserIdAndMockExam_MockExamIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndEventTypeAndIsCorrectFalse(
            Long userId,
            Long mockExamId,
            Long questionId,
            Long attributeId,
            String eventType
    );
}