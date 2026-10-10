package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.AnswerEventRequestDTO;
import com.project.ProjectS.model.AnswerEventResponseDTO;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinalAccountsAutofillResetTest {
    private final QuestionAnswerRepository answers = mock(QuestionAnswerRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final TableAttributeRepository attributes = mock(TableAttributeRepository.class);
    private final QuestionAttributeRepository rows = mock(QuestionAttributeRepository.class);
    private final RuleEngineService rules = mock(RuleEngineService.class);
    private final AnswerEventRepository events = mock(AnswerEventRepository.class);
    private final QuestionAnswerService answerService = new QuestionAnswerService(answers, users, questions,
            mock(TableNameRepository.class), mock(TableHeaderRepository.class), attributes, rows, rules, events);

    @Test
    void finalAccountsResetDeactivatesSavedAnswersAndOnlyPracticeAutofillMarkers() {
        configureQuestion("Drag And Drop", "Final Accounts without Adjustments");
        QuestionAnswer saved = new QuestionAnswer();
        saved.setActiveRow(true);
        when(answers.findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(5L, 1L))
                .thenReturn(List.of(saved));

        assertEquals("Answers reset successfully", answerService.resetAnswersByUserAndQuestion(5L, 1L));

        assertFalse(saved.getActiveRow());
        verify(answers).saveAll(List.of(saved));
        verify(events).deactivatePracticeAutofill(5L, 1L);
        verifyNoMoreInteractions(events);
    }

    @Test
    void resetAlsoClearsAutofillMarkersWhenNoSavedAnswersRemain() {
        configureQuestion("DRAG_AND_DROP", "Final Accounts with Adjustments");
        when(answers.findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(5L, 1L))
                .thenReturn(List.of());

        answerService.resetAnswersByUserAndQuestion(5L, 1L);

        verify(events).deactivatePracticeAutofill(5L, 1L);
        verifyNoMoreInteractions(events);
    }

    @Test
    void journalResetKeepsItsExistingEventBehaviorEvenInAFinalAccountsChapter() {
        configureQuestion("Journal", "Final Accounts with Adjustments");
        when(answers.findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(5L, 1L))
                .thenReturn(List.of());

        answerService.resetAnswersByUserAndQuestion(5L, 1L);

        verify(answers).saveAll(List.of());
        verifyNoInteractions(events);
    }

    @Test
    void otherDragAndDropChaptersDoNotClearEventHistory() {
        configureQuestion("Drag And Drop", "Journal Entries");
        when(answers.findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(5L, 1L))
                .thenReturn(List.of());

        answerService.resetAnswersByUserAndQuestion(5L, 1L);

        verifyNoInteractions(events);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj"})
    void sameCycleAutofillAllowsAValidatedRetryWithoutMarks(String type) {
        AnswerEventService eventService = configureAnswerEventService(new AtomicBoolean(true), type);
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerEventResponseDTO response = assertDoesNotThrow(() -> eventService.createEvent(answerRequest()));

        assertTrue(response.getIsCorrect());
        assertEquals("ANSWER", response.getEventType());
        assertEquals(BigDecimal.ZERO, response.getMarks());
        assertEquals(11L, response.getQuestionAttributeId());
        verify(events).save(any(AnswerEvent.class));
        verify(events, never()).deactivatePracticeAutofill(anyLong(), anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj"})
    void resetThenAnswerAtPositionOneSucceeds(String type) {
        AtomicBoolean autofillActive = new AtomicBoolean(true);
        AnswerEventService eventService = configureAnswerEventService(autofillActive, type);
        when(answers.findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(5L, 1L))
                .thenReturn(List.of());
        when(events.deactivatePracticeAutofill(5L, 1L)).thenAnswer(invocation -> {
            autofillActive.set(false);
            return 1;
        });
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        answerService.resetAnswersByUserAndQuestion(5L, 1L);
        AnswerEventResponseDTO response = assertDoesNotThrow(() -> eventService.createEvent(answerRequest()));

        assertFalse(autofillActive.get());
        assertEquals("ANSWER", response.getEventType());
        assertTrue(response.getIsCorrect());
        assertTrue(response.getActiveRow());
        assertEquals(new BigDecimal("1.00"), response.getMarks());
        assertEquals(11L, response.getQuestionAttributeId());
        verify(events).deactivatePracticeAutofill(5L, 1L);
        verify(events).save(any(AnswerEvent.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj"})
    void autofillAfterResetMakesNewCycleRetriesWorthZero(String type) {
        AtomicBoolean autofillActive = new AtomicBoolean(true);
        AnswerEventService eventService = configureAnswerEventService(autofillActive, type);
        when(answers.findByUser_UserIdAndQuestion_QuestionIdAndActiveRowTrue(5L, 1L))
                .thenReturn(List.of());
        when(events.deactivatePracticeAutofill(5L, 1L)).thenAnswer(invocation -> {
            autofillActive.set(false);
            return 1;
        });
        when(events.save(any())).thenAnswer(invocation -> {
            AnswerEvent event = invocation.getArgument(0);
            if ("AUTOFILL".equals(event.getEventType())) {
                autofillActive.set(true);
            }
            return event;
        });

        answerService.resetAnswersByUserAndQuestion(5L, 1L);
        AnswerEventRequestDTO autofill = answerRequest();
        autofill.setEventType("AUTOFILL");
        assertDoesNotThrow(() -> eventService.createEvent(autofill));

        AnswerEventResponseDTO retry = assertDoesNotThrow(() -> eventService.createEvent(answerRequest()));
        assertTrue(retry.getIsCorrect());
        assertEquals(BigDecimal.ZERO, retry.getMarks());
        verify(events, times(2)).save(any(AnswerEvent.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj"})
    void retryAfterAutofillStillRejectsAnIncorrectPlacement(String type) {
        AnswerEventService eventService = configureAnswerEventService(new AtomicBoolean(true), type);
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Question question = questions.findById(1L).orElseThrow();
        QuestionAttribute row = rows.findByQuestion_QuestionId(1L).get(0);
        when(rules.validatesFinalAccountsPlacement(question, row, 1L, 2L, "add", new BigDecimal("200"), 1L))
                .thenReturn(false);

        AnswerEventResponseDTO response = eventService.createEvent(answerRequest());

        assertFalse(response.getIsCorrect());
        assertEquals(BigDecimal.ZERO, response.getMarks());
        verify(events, never()).deactivatePracticeAutofill(anyLong(), anyLong());
    }

    @Test
    void unrelatedQuestionTypesKeepTheirExistingAutofillGuard() {
        AnswerEventService eventService = configureAnswerEventService(new AtomicBoolean(true), "Drag And Drop");
        configureQuestion("Drag And Drop", "Journal Entries");
        when(events.existsByUser_UserIdAndQuestion_QuestionIdAndAttribute_AttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                5L, 1L, 8L, 1, "AUTOFILL")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> eventService.createEvent(answerRequest()));
        verify(events, never()).save(any());
    }

    @Test
    void markerUpdateIsScopedToActivePracticeAutofillForOneUserAndQuestion() throws Exception {
        var method = AnswerEventRepository.class.getMethod("deactivatePracticeAutofill", Long.class, Long.class);
        assertNotNull(method.getAnnotation(Modifying.class));
        Query annotation = method.getAnnotation(Query.class);
        assertNotNull(annotation);
        String query = annotation.value().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        assertTrue(query.contains(".user.userid = :userid"));
        assertTrue(query.contains(".question.questionid = :questionid"));
        assertTrue(query.contains(".eventtype = 'autofill'"));
        assertTrue(query.contains(".activerow = true"));
        assertTrue(query.contains(".exam is null"));
        assertTrue(query.contains(".mockexam is null"));
        assertTrue(query.contains("set ") && query.contains(".activerow = false"));
    }

    private Question configureQuestion(String type, String chapterName) {
        User lockedUser = new User(); lockedUser.setUserId(5L);
        when(users.findByUserIdForUpdate(5L)).thenReturn(Optional.of(lockedUser));
        Question question = new Question();
        question.setQuestionId(1L);
        QuestionType questionType = new QuestionType(); questionType.setQuestionType(type);
        question.setQuestionType(questionType);
        Chapter chapter = new Chapter(); chapter.setChapterId(1L); chapter.setName(chapterName);
        question.setChapter(chapter);
        when(questions.findById(1L)).thenReturn(Optional.of(question));
        return question;
    }

    private AnswerEventService configureAnswerEventService(AtomicBoolean autofillActive, String type) {
        Question question = configureQuestion(type, "Final Accounts without Adjustments");
        User user = new User(); user.setUserId(5L);
        when(users.findById(5L)).thenReturn(Optional.of(user));
        TableAttribute attribute = new TableAttribute(); attribute.setAttributeId(8L); attribute.setName("Sales");
        when(attributes.findById(8L)).thenReturn(Optional.of(attribute));
        QuestionAttribute row = new QuestionAttribute(); row.setQuestionAttributeId(11L);
        row.setQuestion(question); row.setAttribute(attribute); row.setAmount(new BigDecimal("200"));
        when(rows.findByQuestion_QuestionId(1L)).thenReturn(List.of(row));
        when(rules.validatesFinalAccountsPlacement(question, row, 1L, 2L, "add", new BigDecimal("200"), 1L))
                .thenReturn(true);
        when(events.existsByUser_UserIdAndQuestion_QuestionIdAndQuestionAttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                5L, 1L, 11L, 1, "AUTOFILL")).thenAnswer(invocation -> autofillActive.get());
        return new AnswerEventService(events, users, questions, attributes,
                mock(QuestionFillBlankAnswerRepository.class), rows, rules);
    }

    private AnswerEventRequestDTO answerRequest() {
        AnswerEventRequestDTO request = new AnswerEventRequestDTO();
        request.setUserId(5L); request.setQuestionId(1L); request.setQuestionAttributeId(11L);
        request.setAttributeId(8L); request.setAnswerPosition(1); request.setEventType("ANSWER");
        request.setTableNameId(1L); request.setHeaderId(2L); request.setConditionId(1L);
        request.setAmount(new BigDecimal("200")); request.setArithmetic("add"); request.setIsCorrect(true);
        return request;
    }

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj"})
    void practiceMarksFollowTheSameAttemptTableAndContributeToOverallScore(String type) {
        AnswerEventService service = configureAnswerEventService(new AtomicBoolean(false), type);
        List<AnswerEvent> savedEvents = new ArrayList<>();
        when(events.save(any())).thenAnswer(invocation -> {
            AnswerEvent event = invocation.getArgument(0);
            savedEvents.add(event);
            return event;
        });
        Question question = questions.findById(1L).orElseThrow();
        QuestionAttribute row = rows.findByQuestion_QuestionId(1L).get(0);
        for (int attempt = 1; attempt <= 3; attempt++) {
            when(events.countByUser_UserIdAndQuestion_QuestionIdAndQuestionAttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                    5L, 1L, 11L, 1, "ANSWER")).thenReturn((long) attempt - 1);
            for (boolean correct : new boolean[]{false, true}) {
                when(rules.validatesFinalAccountsPlacement(question, row, 1L, 2L, "add", new BigDecimal("200"), 1L))
                        .thenReturn(correct);
                AnswerEventResponseDTO response = service.createEvent(answerRequest());
                BigDecimal expected = !correct || attempt >= 3 ? BigDecimal.ZERO
                        : new BigDecimal(attempt == 1 ? "1.00" : "0.50");
                assertEquals(expected, response.getMarks());
                assertEquals(attempt, response.getAttemptNumber());
            }
        }
        for (String assistance : List.of("HINT", "AUTOFILL")) {
            AnswerEventRequestDTO request = answerRequest();
            request.setEventType(assistance);
            assertEquals(BigDecimal.ZERO, service.createEvent(request).getMarks());
        }
        when(events.findByUser_UserIdAndExamIsNullAndMockExamIsNull(5L)).thenReturn(savedEvents);
        assertEquals(new BigDecimal("1.50"), service.getOverallMarks(5L));
    }
}
