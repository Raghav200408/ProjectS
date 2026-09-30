package com.project.ProjectS.repository;

import com.project.ProjectS.entity.PracticeResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticeResultRepository
        extends JpaRepository<PracticeResult, Long> {

    /*
     * Insert-or-update in one statement, so two simultaneous attempts on the
     * same unit can't race into a duplicate row (or a unique violation).
     * The ON CONFLICT targets are the partial unique indexes from
     * create_practice_results.sql. A repeat attempt overwrites is_correct,
     * bumps attempt_number and refreshes the hierarchy ids; created_at stays.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO practice_results (
                user_id, question_id, question_attribute_id,
                course_id, subject_id, chapter_id, topic_id,
                question_type, is_correct, attempt_number,
                answered_at, created_at, updated_at)
            VALUES (
                :userId, :questionId, :questionAttributeId,
                :courseId, :subjectId, :chapterId, :topicId,
                'ATTRIBUTE', :isCorrect, 1,
                now(), now(), now())
            ON CONFLICT (user_id, question_id, question_attribute_id)
                WHERE question_attribute_id IS NOT NULL
            DO UPDATE SET
                is_correct     = EXCLUDED.is_correct,
                attempt_number = practice_results.attempt_number + 1,
                course_id      = EXCLUDED.course_id,
                subject_id     = EXCLUDED.subject_id,
                chapter_id     = EXCLUDED.chapter_id,
                topic_id       = EXCLUDED.topic_id,
                answered_at    = now(),
                updated_at     = now()
            """, nativeQuery = true)
    int upsertAttributeResult(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId,
            @Param("questionAttributeId") Long questionAttributeId,
            @Param("courseId") Long courseId,
            @Param("subjectId") Long subjectId,
            @Param("chapterId") Long chapterId,
            @Param("topicId") Long topicId,
            @Param("isCorrect") boolean isCorrect
    );

    // Same, for an MCQ where the question itself is the unit.
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO practice_results (
                user_id, question_id, question_attribute_id,
                course_id, subject_id, chapter_id, topic_id,
                question_type, is_correct, attempt_number,
                answered_at, created_at, updated_at)
            VALUES (
                :userId, :questionId, NULL,
                :courseId, :subjectId, :chapterId, :topicId,
                'MCQ', :isCorrect, 1,
                now(), now(), now())
            ON CONFLICT (user_id, question_id)
                WHERE question_attribute_id IS NULL
            DO UPDATE SET
                is_correct     = EXCLUDED.is_correct,
                attempt_number = practice_results.attempt_number + 1,
                course_id      = EXCLUDED.course_id,
                subject_id     = EXCLUDED.subject_id,
                chapter_id     = EXCLUDED.chapter_id,
                topic_id       = EXCLUDED.topic_id,
                answered_at    = now(),
                updated_at     = now()
            """, nativeQuery = true)
    int upsertQuestionResult(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId,
            @Param("courseId") Long courseId,
            @Param("subjectId") Long subjectId,
            @Param("chapterId") Long chapterId,
            @Param("topicId") Long topicId,
            @Param("isCorrect") boolean isCorrect
    );

    Optional<PracticeResult>
    findByUser_UserIdAndQuestion_QuestionIdAndQuestionAttribute_QuestionAttributeId(
            Long userId,
            Long questionId,
            Long questionAttributeId
    );

    Optional<PracticeResult>
    findByUser_UserIdAndQuestion_QuestionIdAndQuestionAttributeIsNull(
            Long userId,
            Long questionId
    );

    // Used by the practice Reset button, which starts the question over.
    @Modifying
    @Query("""
            DELETE FROM PracticeResult pr
            WHERE pr.user.userId = :userId
              AND pr.question.questionId = :questionId
            """)
    int deleteByUserAndQuestion(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId
    );
}
