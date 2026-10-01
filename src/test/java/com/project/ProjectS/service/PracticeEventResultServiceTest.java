package com.project.ProjectS.service;

import com.project.ProjectS.entity.AnswerEvent;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.entity.QuestionAttribute;
import com.project.ProjectS.model.PracticeEventResultRequestDTO;
import com.project.ProjectS.repository.AnswerEventRepository;
import com.project.ProjectS.repository.QuestionAttributeRepository;
import com.project.ProjectS.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PracticeEventResultServiceTest {
    private final AnswerEventRepository events = mock(AnswerEventRepository.class);
    private final QuestionAttributeRepository attributes = mock(QuestionAttributeRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final Authentication authentication = mock(Authentication.class);
    private final User user = mock(User.class, RETURNS_DEEP_STUBS);
    private final AnswerEvent event = mock(AnswerEvent.class, RETURNS_DEEP_STUBS);
    private final PracticeEventResultRequestDTO request = new PracticeEventResultRequestDTO();
    private final PracticeEventResultService service = new PracticeEventResultService(events, attributes, users, jdbc);

    @BeforeEach
    void setup() {
        request.setAnswerEventId(10L);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("student@example.com");
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(user.getUserId()).thenReturn(1L);
        when(user.getRole().getRoleName()).thenReturn("STUDENT");
        when(events.findById(10L)).thenReturn(Optional.of(event));
        when(event.getUser().getUserId()).thenReturn(1L);
        when(event.getExam()).thenReturn(null);
        when(event.getMockExam()).thenReturn(null);
        when(event.getActiveRow()).thenReturn(true);
        when(event.getIsCorrect()).thenReturn(false);
        when(event.getEventType()).thenReturn("ANSWER");
        when(event.getQuestion().getActiveRow()).thenReturn(true);
        when(event.getQuestion().getQuestionType().getQuestionType()).thenReturn("FILL_IN_THE_BLANKS");
        when(event.getQuestion().getQuestionId()).thenReturn(2L);
        when(event.getAnswerPosition()).thenReturn(1);
    }

    @Test
    void rejectsAnotherStudentsEvent() {
        when(event.getUser().getUserId()).thenReturn(99L);
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.record(request, authentication)).getStatusCode().value());
        verifyNoInteractions(jdbc);
    }

    @Test
    void rejectsExamEvents() {
        when(event.getExam()).thenReturn(mock(com.project.ProjectS.entity.Exam.class));
        assertThrows(ResponseStatusException.class, () -> service.record(request, authentication));
        verifyNoInteractions(jdbc);
    }

    @Test
    void doesNotCountHintRequestsOrAutofill() {
        when(event.getIsCorrect()).thenReturn(null);
        assertFalse(service.record(request, authentication));
        when(event.getIsCorrect()).thenReturn(true);
        when(event.getEventType()).thenReturn("AUTOFILL");
        assertFalse(service.record(request, authentication));
        verifyNoInteractions(jdbc);
    }

    @Test
    void recordsWrongBlankAndTreatsRepeatedProjectionAsNoChange() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(2L), eq(1))).thenReturn(1L);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1, 0);
        assertTrue(service.record(request, authentication));
        assertFalse(service.record(request, authentication));
        verify(jdbc, times(2)).update(contains("last_answer_event_id < EXCLUDED.last_answer_event_id"), any(Object[].class));
    }

    @Test
    void resetDoesNotDiscardAnInFlightPracticeResult() {
        when(event.getActiveRow()).thenReturn(false);
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(2L), eq(1))).thenReturn(1L);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertTrue(service.record(request, authentication));
    }

    @Test
    void matchingUsesSourcePairRatherThanSelectedTarget() {
        when(event.getQuestion().getQuestionType().getQuestionType()).thenReturn("MATCH_THE_FOLLOWING");
        request.setUnitPosition(7);
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(2L), eq(7))).thenReturn(1L);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertTrue(service.record(request, authentication));
        verify(jdbc).queryForObject(contains("pair_id = ?"), eq(Long.class), eq(2L), eq(7));
    }

    @Test
    void attributePositionsAreStoredAsSeparateUnits() {
        when(event.getQuestion().getQuestionType().getQuestionType()).thenReturn("JOURNAL");
        when(event.getAttribute().getAttributeId()).thenReturn(3L);
        QuestionAttribute attribute = mock(QuestionAttribute.class, RETURNS_DEEP_STUBS);
        when(attribute.getActiveRow()).thenReturn(true);
        when(attribute.getAttribute().getAttributeId()).thenReturn(3L);
        when(attribute.getQuestionAttributeId()).thenReturn(7L);
        when(attributes.findByQuestion_QuestionId(2L)).thenReturn(List.of(attribute));
        List<Object> savedPositions = new ArrayList<>();
        when(jdbc.update(anyString(), any(Object[].class))).thenAnswer(invocation -> {
            Object[] values = (Object[]) invocation.getRawArguments()[1];
            assertEquals(3L, values[2]);
            savedPositions.add(values[3]);
            return 1;
        });
        assertTrue(service.record(request, authentication));
        when(event.getAnswerPosition()).thenReturn(2);
        assertTrue(service.record(request, authentication));
        // Journal/dropdown retain null positions in answer_events; use practice metadata.
        when(event.getAnswerPosition()).thenReturn(null);
        request.setUnitPosition(3);
        assertTrue(service.record(request, authentication));
        request.setUnitPosition(4);
        assertTrue(service.record(request, authentication));
        request.setUnitPosition(null);
        assertThrows(ResponseStatusException.class, () -> service.record(request, authentication));
        request.setUnitPosition(3);
        when(event.getAnswerPosition()).thenReturn(2);
        assertThrows(ResponseStatusException.class, () -> service.record(request, authentication));
        assertEquals(List.of(1, 2, 3, 4), savedPositions);
    }
}
