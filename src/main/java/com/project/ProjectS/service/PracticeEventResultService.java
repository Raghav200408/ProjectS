package com.project.ProjectS.service;

import com.project.ProjectS.entity.AnswerEvent;
import com.project.ProjectS.entity.QuestionAttribute;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.PracticeEventResultRequestDTO;
import com.project.ProjectS.repository.AnswerEventRepository;
import com.project.ProjectS.repository.QuestionAttributeRepository;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Projects an already saved practice event without creating a second event. */
@Service
public class PracticeEventResultService {
    private static final Logger log = LogManager.getLogger(PracticeEventResultService.class);
    private final AnswerEventRepository events;
    private final QuestionAttributeRepository attributes;
    private final UserRepository users;
    private final JdbcTemplate jdbc;

    public PracticeEventResultService(AnswerEventRepository events,
            QuestionAttributeRepository attributes, UserRepository users, JdbcTemplate jdbc) {
        this.events = events;
        this.attributes = attributes;
        this.users = users;
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean record(PracticeEventResultRequestDTO request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        User user = users.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (request.getAnswerEventId() == null) {
            throw badRequest("answerEventId is required");
        }
        AnswerEvent event = events.findById(request.getAnswerEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer event not found"));
        if (!Objects.equals(user.getUserId(), event.getUser().getUserId())
                || "GUEST".equals(user.getRole().getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (event.getExam() != null || event.getMockExam() != null) {
            throw badRequest("Exam events cannot be recorded as practice");
        }
        // A hint request has no correctness; autofill reveals rather than attempts an answer.
        // An event deactivated by Reset still represents an attempt. This also
        // allows an in-flight practice write to finish after the answer cycle resets.
        if (event.getIsCorrect() == null
                || "AUTOFILL".equals(event.getEventType())) return false;
        if (!"ANSWER".equals(event.getEventType()) && !"HINT".equals(event.getEventType())) return false;

        var question = event.getQuestion();
        if (!Boolean.TRUE.equals(question.getActiveRow())) throw badRequest("Question is inactive");
        String typeName = question.getQuestionType().getQuestionType().toUpperCase(Locale.ROOT);
        Long attributeId = null;
        Integer position = null;
        String type;
        if (typeName.contains("FILL")) {
            type = "FILL_BLANK";
            position = event.getAnswerPosition();
            Long count = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM question_fill_blank_answers
                    WHERE question_id = ? AND COALESCE(blank_number, 1) = ?
                    """, Long.class, question.getQuestionId(), position);
            if (position == null || count == null || count == 0) throw badRequest("Unknown blank position");
        } else if (typeName.contains("MATCH")) {
            type = "MATCHING";
            // Existing answer events keep their target position unchanged.
            position = request.getUnitPosition();
            Long count = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM question_matching_pairs WHERE question_id = ? AND pair_id = ?
                    """, Long.class, question.getQuestionId(), position);
            if (position == null || count == null || count == 0) throw badRequest("Unknown matching source pair");
        } else if (Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM mcq_questions WHERE question_id = ?)",
                Boolean.class, question.getQuestionId()))) {
            // MCQ submission already records the server-scored result.
            return false;
        } else {
            type = "ATTRIBUTE";
            List<QuestionAttribute> candidates = attributes.findByQuestion_QuestionId(question.getQuestionId())
                    .stream().filter(a -> Boolean.TRUE.equals(a.getActiveRow()))
                    .filter(a -> event.getAttribute() != null && Objects.equals(
                            a.getAttribute().getAttributeId(), event.getAttribute().getAttributeId()))
                    .toList();
            if (candidates.isEmpty()) throw badRequest("Attribute does not belong to this active question");
            attributeId = event.getAttribute().getAttributeId();
            // Journal/dropdown events historically omit the position. The
            // separate practice request supplies it without changing those events.
            position = event.getAnswerPosition() != null
                    ? event.getAnswerPosition() : request.getUnitPosition();
            if (event.getAnswerPosition() != null && request.getUnitPosition() != null
                    && !Objects.equals(event.getAnswerPosition(), request.getUnitPosition())) {
                throw badRequest("Practice position does not match the saved event");
            }
            if (position == null || position < 1 || position > 4) {
                throw badRequest("Attribute answerPosition must be between 1 and 4");
            }
        }
        return jdbc.update("""
                INSERT INTO practice_results (user_id, question_id, attribute_id, answer_position,
                    course_id, subject_id, chapter_id, topic_id, question_type, is_correct,
                    attempt_number, last_answer_event_id, answered_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, now(), now())
                ON CONFLICT (user_id, question_id, unit_key) DO UPDATE SET
                    is_correct = EXCLUDED.is_correct,
                    attempt_number = practice_results.attempt_number + 1,
                    last_answer_event_id = EXCLUDED.last_answer_event_id,
                    answered_at = EXCLUDED.answered_at, updated_at = now(),
                    course_id = EXCLUDED.course_id, subject_id = EXCLUDED.subject_id,
                    chapter_id = EXCLUDED.chapter_id, topic_id = EXCLUDED.topic_id
                WHERE practice_results.last_answer_event_id IS NULL
                   OR practice_results.last_answer_event_id < EXCLUDED.last_answer_event_id
                """, user.getUserId(), question.getQuestionId(), attributeId, position,
                question.getCourse().getCourseId(), question.getSubject().getSubjectId(),
                question.getChapter().getChapterId(), question.getTopic().getTopicId(),
                type, event.getIsCorrect(), event.getAnswerEventId(), event.getCreatedAt()) > 0;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
