package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinalAccountsDragDropTest {
    private final QuestionAttributeRepository rows = mock(QuestionAttributeRepository.class);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final RuleEngineService rules = mock(RuleEngineService.class);
    private final AnswerEventRepository events = mock(AnswerEventRepository.class);
    private final ExamScoringService scorer = new ExamScoringService(rows, mock(McqOptionRepository.class), rules,
            mock(TableNameRepository.class), mock(TableHeaderRepository.class), questions, events);

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj", "DRAG_AND_DROP_WITH_ADJ"})
    void finalAccountsRequiresEveryAdjustmentEffectOnce(String type) {
        Question question = question(type, "Final Accounts with Adjustments");
        QuestionAttribute row = row(question, 11L, 8L, "200", "70");
        configure(question, List.of(row), rule(condition(1L, 2L, "1"), condition(2L, 4L, "2")));
        ExamAnswerDTO trading = answer(11L, 8L, 1L, 2L, "200");
        ExamAnswerDTO asset = answer(11L, 8L, 2L, 4L, "70");

        assertEquals(0, score(trading).totalMarks());
        assertEquals(0, score(trading, trading).totalMarks());
        assertEquals(1, score(trading, asset).totalMarks());
        assertEquals(0, score(trading, asset, asset).totalMarks());
        assertEquals(0, score(trading, asset, answer(999L, 8L, 1L, 2L, "200")).totalMarks());
        assertFalse(score(trading).questionScores().get(0).correct());
        verify(rules, atLeastOnce()).getRuleEngineByAttributeId(8L, 1L);
        verify(rules, never()).getRuleEngineByAttributeId(8L);
    }

    @Test
    void repeatedAccountsKeepSeparateAmountsAndMaximumMarks() {
        Question question = question("DRAG_AND_DROP", "Final Accounts without Adjustments");
        configure(question, List.of(row(question, 11L, 8L, "200", null), row(question, 12L, 8L, "300", null)),
                rule(condition(1L, 2L, "1")));

        assertEquals(2, score(answer(11L, 8L, 1L, 2L, "200"), answer(12L, 8L, 1L, 2L, "300")).totalMarks());
        ExamScoringService.Score missing = score(answer(11L, 8L, 1L, 2L, "200"));
        assertEquals(2, missing.maximumMarks());
        assertEquals(1, missing.totalMarks());
        assertFalse(missing.questionScores().get(0).correct());
        assertEquals(0, score(answer(11L, 8L, 1L, 2L, "300"), answer(12L, 8L, 1L, 2L, "200")).totalMarks());
        assertEquals(0, score(answer(null, 8L, 1L, 2L, "200")).totalMarks());
    }

    @Test
    void unansweredFinalAccountsQuestionStillHasMaximumMarks() {
        Question question = question("Drag And Drop", "Final Accounts without Adjustments");
        configure(question, List.of(row(question, 11L, 8L, "200", null)), rule(condition(1L, 2L, "1")));
        ExamScoringService.Score empty = scorer.score(List.of(1L), List.of());
        assertEquals(1, empty.maximumMarks());
        assertFalse(empty.questionScores().get(0).correct());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj"})
    void reviewRetainsSecondAmountAndQuestionRowIdentity(String type) {
        Question question = question(type, "Final Accounts with Adjustments");
        configure(question, List.of(row(question, 11L, 8L, "200", "70")),
                rule(condition(1L, 2L, "1"), condition(2L, 4L, "2")));
        ExamQuestionAnswerDTO submission = submission(answer(11L, 8L, 1L, 2L, "200"), answer(11L, 8L, 2L, 4L, "70"));
        User user = new User(); user.setUserId(5L);
        Exam exam = new Exam(); exam.setExamId(10L);
        scorer.persistAnswerInfo(user, exam, null, List.of(submission), score(submission.getAnswers().toArray(ExamAnswerDTO[]::new)).questionScores());
        ArgumentCaptor<AnswerEvent> saved = ArgumentCaptor.forClass(AnswerEvent.class);
        verify(events, times(2)).save(saved.capture());
        when(events.findByUser_UserIdAndExam_ExamIdAndEventType(5L, 10L, "EXAM_SUBMIT"))
                .thenReturn(saved.getAllValues());

        ExamReviewQuestionDTO review = scorer.buildReviewFromAnswerInfo(5L, 10L, null).get(0);
        assertTrue(review.getCorrect());
        assertEquals(type.equals("dragAndDropWithAdj") ? "DRAG_AND_DROP_WITH_ADJ" : "DRAG_AND_DROP", review.getQuestionType());
        assertEquals("70", review.getAnswers().get(1).getAnsweredData().get("amount").toString());
        assertEquals("11", review.getAnswers().get(1).getAnsweredData().get("questionAttributeId").toString());
        assertEquals(11L, saved.getAllValues().get(1).getQuestionAttributeId());
        assertTrue(saved.getAllValues().get(1).getDescription().startsWith("attempted to"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Drag And Drop", "dragAndDropWithAdj"})
    void practiceCorrectnessComesFromRuleValidationAndAttemptsUseQuestionRow(String type) {
        Question question = question(type, "Final Accounts without Adjustments");
        QuestionAttribute row = row(question, 11L, 8L, "200", null);
        configure(question, List.of(row), rule(condition(1L, 2L, "1")));
        UserRepository users = mock(UserRepository.class);
        TableAttributeRepository attributes = mock(TableAttributeRepository.class);
        User user = new User(); user.setUserId(5L);
        when(users.findById(5L)).thenReturn(Optional.of(user));
        when(attributes.findById(8L)).thenReturn(Optional.of(row.getAttribute()));
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AnswerEventService service = new AnswerEventService(events, users, questions, attributes,
                mock(QuestionFillBlankAnswerRepository.class), rows, rules);
        AnswerEventRequestDTO request = new AnswerEventRequestDTO();
        request.setUserId(5L); request.setQuestionId(1L); request.setQuestionAttributeId(11L);
        request.setAttributeId(8L); request.setAnswerPosition(1); request.setEventType("ANSWER");
        request.setTableNameId(2L); request.setHeaderId(4L); request.setAmount(new BigDecimal("200"));
        request.setArithmetic("add"); request.setIsCorrect(true);

        AnswerEventResponseDTO response = service.createEvent(request);
        assertFalse(response.getIsCorrect());
        assertEquals(BigDecimal.ZERO, response.getMarks());
        verify(rules).validatesFinalAccountsPlacement(question, row, 2L, 4L, "add", new BigDecimal("200"), null);
        verify(events).countByUser_UserIdAndQuestion_QuestionIdAndQuestionAttributeIdAndAnswerPositionAndEventTypeAndActiveRowTrue(
                5L, 1L, 11L, 1, "ANSWER");
    }

    @Test
    void journalAndOtherChaptersStayOnExistingScoringPath() {
        Question question = question("Journal", "Final Accounts with Adjustments");
        QuestionAttribute row = row(question, 11L, 8L, "200", "70");
        configure(question, List.of(row), rule(condition(1L, 2L, "1"), condition(2L, 4L, "2")));
        when(rules.getRuleEngineByAttributeId(8L)).thenReturn(List.of(rule(condition(1L, 2L, "1"))));
        TableName table = new TableName(); table.setTableNameId(1L); table.setName("Trading Account");
        TableHeader header = new TableHeader(); header.setHeaderId(2L); header.setName("Credit Particulars");
        TableNameRepository names = mock(TableNameRepository.class);
        TableHeaderRepository headers = mock(TableHeaderRepository.class);
        when(names.findAll()).thenReturn(List.of(table)); when(headers.findAll()).thenReturn(List.of(header));
        ExamScoringService oldPath = new ExamScoringService(rows, mock(McqOptionRepository.class), rules,
                names, headers, questions, events);

        assertEquals(1, oldPath.score(List.of(1L), List.of(submission(answer(11L, 8L, 1L, 2L, "200")))).totalMarks());
        verify(rules, never()).getRuleEngineByAttributeId(8L, 1L);
        assertFalse(RuleEngineService.isFinalAccountsDragDrop(question("Drag And Drop", "Journal Entries")));
    }

    private void configure(Question question, List<QuestionAttribute> questionRows, RuleEngineResponse rule) {
        when(questions.findById(1L)).thenReturn(Optional.of(question));
        when(rows.findByQuestion_QuestionId(1L)).thenReturn(questionRows);
        when(rules.getRuleEngineByAttributeId(8L, 1L)).thenReturn(List.of(rule));
    }

    @Test
    void adjustmentTypePreservesAttributeHeaderValidationAndUsesRulesInRenamedChapters() {
        Question question = question("dragAndDropWithAdj", "Accounts assessment");
        assertTrue(RuleEngineService.isFinalAccountsDragDrop(question));
        TableHeader debit = new TableHeader(); debit.setHeaderId(10L);
        TableHeader adjustment = new TableHeader(); adjustment.setHeaderId(11L);
        TableAttribute attribute = new TableAttribute(); attribute.setName("Adjustment"); attribute.setTableHeader(adjustment);
        assertDoesNotThrow(() -> QuestionService.validateAttributeHeader(question, adjustment, attribute));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> QuestionService.validateAttributeHeader(question, debit, attribute));
    }

    private ExamScoringService.Score score(ExamAnswerDTO... answers) {
        return scorer.score(List.of(1L), List.of(submission(answers)));
    }

    private ExamQuestionAnswerDTO submission(ExamAnswerDTO... answers) {
        ExamQuestionAnswerDTO dto = new ExamQuestionAnswerDTO();
        dto.setQuestionId(1L); dto.setQuestionType("DRAG_AND_DROP"); dto.setAnswers(List.of(answers));
        return dto;
    }

    private Question question(String type, String chapterName) {
        Question question = new Question(); question.setQuestionId(1L);
        QuestionType questionType = new QuestionType(); questionType.setQuestionType(type); question.setQuestionType(questionType);
        Chapter chapter = new Chapter(); chapter.setChapterId(1L); chapter.setName(chapterName); question.setChapter(chapter);
        return question;
    }

    private QuestionAttribute row(Question question, Long rowId, Long attributeId, String amount, String amount2) {
        QuestionAttribute row = new QuestionAttribute(); row.setQuestion(question); row.setQuestionAttributeId(rowId);
        TableAttribute attribute = new TableAttribute(); attribute.setAttributeId(attributeId); attribute.setName("Sales");
        row.setAttribute(attribute); row.setAmount(new BigDecimal(amount));
        row.setAmount2(amount2 == null ? null : new BigDecimal(amount2));
        return row;
    }

    private RuleConditionDTO condition(Long tableId, Long headerId, String selector) {
        RuleConditionDTO condition = new RuleConditionDTO(); condition.setTableId(tableId); condition.setHeaderId(headerId);
        condition.setArithmetic("add"); condition.setAmountPosition(selector); return condition;
    }

    private RuleEngineResponse rule(RuleConditionDTO... conditions) {
        RuleEngineResponse rule = new RuleEngineResponse(); rule.setCondition1(conditions[0]);
        if (conditions.length > 1) rule.setCondition2(conditions[1]);
        return rule;
    }

    private ExamAnswerDTO answer(Long rowId, Long attributeId, Long tableId, Long headerId, String amount) {
        Map<String, Object> data = new HashMap<>(); data.put("questionAttributeId", rowId); data.put("attributeId", attributeId);
        data.put("tableNameId", tableId); data.put("headerId", headerId); data.put("arithmetic", "add");
        data.put("tableName", "Trading Account"); data.put("headerName", "Credit Particulars");
        data.put("amount", new BigDecimal(amount)); data.put("info", "attempted to ADD on Credit Particulars of Trading Account.");
        ExamAnswerDTO answer = new ExamAnswerDTO(); answer.setAnsweredData(data); return answer;
    }
}
