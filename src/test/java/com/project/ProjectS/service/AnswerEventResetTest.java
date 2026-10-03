package com.project.ProjectS.service;

import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AnswerEventResetTest {
    @Test
    void resetOnlyDeactivatesTheAnswerCycle() {
        AnswerEventRepository events = mock(AnswerEventRepository.class);
        UserRepository users = mock(UserRepository.class);
        QuestionRepository questions = mock(QuestionRepository.class);
        TableAttributeRepository attributes = mock(TableAttributeRepository.class);
        QuestionFillBlankAnswerRepository blanks = mock(QuestionFillBlankAnswerRepository.class);
        AnswerEventService service = new AnswerEventService(events, users, questions, attributes, blanks);
        when(events.deactivateByUserAndQuestion(5L, 21L)).thenReturn(3);

        assertEquals(3, service.resetEvents(5L, 21L));

        verify(events).deactivateByUserAndQuestion(5L, 21L);
        verifyNoMoreInteractions(events);
        verifyNoInteractions(users, questions, attributes, blanks);
    }
}
