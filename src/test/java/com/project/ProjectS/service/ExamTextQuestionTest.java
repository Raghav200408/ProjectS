package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Match the following and fill in the blanks in exams and mock exams:
// scoring, max marks, saved rows, the review and the paper.
class ExamTextQuestionTest {
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final AnswerEventRepository events = mock(AnswerEventRepository.class);
    private final QuestionMatchingPairRepository pairs = mock(QuestionMatchingPairRepository.class);
    private final QuestionFillBlankAnswerRepository blanks = mock(QuestionFillBlankAnswerRepository.class);
    private final ExamScoringService scorer = new ExamScoringService(
            mock(QuestionAttributeRepository.class), mock(McqOptionRepository.class),
            mock(RuleEngineService.class), mock(TableNameRepository.class),
            mock(TableHeaderRepository.class), questions, events, pairs, blanks);

    // ---------------------------------------------------------------
    // MATCH THE FOLLOWING
    // ---------------------------------------------------------------

    @Test
    void matchingEarnsOneMarkForEachRightPair() {
        Question question = matchingQuestion();

        ExamScoringService.Score score = scorer.score(List.of(1L), List.of(submission(
                "DRAG_AND_DROP", // the browser's type is ignored
                match(10L, "  asset  "),   // spaces and case do not matter
                match(11L, "Revenue"),
                match(12L, "Asset"))));    // wrong

        assertEquals(2, score.totalMarks());
        assertEquals(3, score.maximumMarks());
        assertFalse(score.questionScores().get(0).correct());
        assertEquals(question.getQuestionId(), score.questionScores().get(0).questionId());
    }

    @Test
    void matchingCountsOnlyTheFirstLineForAPair() {
        matchingQuestion();

        ExamScoringService.Score score = scorer.score(List.of(1L), List.of(submission(
                "MATCHING", match(10L, "Asset"), match(10L, "Asset"), match(10L, "Asset"))));

        assertEquals(1, score.totalMarks());
    }

    @Test
    void matchingAllRightIsCorrect() {
        matchingQuestion();

        ExamScoringService.Score score = scorer.score(List.of(1L), List.of(submission(
                "MATCHING", match(10L, "Asset"), match(11L, "Revenue"), match(12L, "Liability"))));

        assertEquals(3, score.totalMarks());
        assertTrue(score.questionScores().get(0).correct());
    }

    @Test
    void unattemptedMatchingStillCountsItsMaximum() {
        matchingQuestion();

        ExamScoringService.Score score = scorer.score(List.of(1L), List.of(submission("MATCHING")));

        assertEquals(0, score.totalMarks());
        assertEquals(3, score.maximumMarks());
    }

    // ---------------------------------------------------------------
    // FILL IN THE BLANKS
    // ---------------------------------------------------------------

    @Test
    void fillBlankEarnsOneMarkForEachRightBlank() {
        fillBlankQuestion("Cash is an ___ and a loan is a ___.", List.of(
                option(1, 1, "Asset", true), option(2, 1, "Liability", false),
                option(3, 2, "Liability", true), option(4, 2, "Asset", false)));

        ExamScoringService.Score score = scorer.score(List.of(1L), List.of(submission(
                "FILL_IN_THE_BLANK", blank(1, "asset"), blank(2, "Asset"))));

        assertEquals(1, score.totalMarks());
        assertEquals(2, score.maximumMarks());
    }

    @Test
    void fillBlankAcceptsTheSameWordInTwoBlanks() {
        fillBlankQuestion("___ and ___", List.of(
                option(1, 1, "Debit", true), option(2, 1, "Credit", false),
                option(3, 2, "Debit", true), option(4, 2, "Credit", false)));

        ExamScoringService.Score score = scorer.score(List.of(1L), List.of(submission(
                "FILL_IN_THE_BLANK", blank(1, "Debit"), blank(2, "Debit"))));

        assertEquals(2, score.totalMarks());
        assertTrue(score.questionScores().get(0).correct());
    }

    @Test
    void anOptionWithNoBlankNumberGoesToBlankOneOnlyInAOneBlankSentence() {
        fillBlankQuestion("Cash is an ___.", List.of(
                option(1, null, "Asset", true), option(2, null, "Liability", false)));

        assertEquals(1, scorer.computeQuestionMaxMarks(1L));
        assertEquals(1, scorer.score(List.of(1L), List.of(submission(
                "FILL_IN_THE_BLANK", blank(1, "Asset")))).totalMarks());

        fillBlankQuestion("___ and ___", List.of(
                option(1, null, "Asset", true), option(2, 2, "Liability", true)));

        assertEquals(1, scorer.computeQuestionMaxMarks(1L));
    }

    // ---------------------------------------------------------------
    // MAX MARKS
    // ---------------------------------------------------------------

    @Test
    void maxMarksIsOneForEachPairOrBlank() {
        matchingQuestion();
        assertEquals(3, scorer.computeQuestionMaxMarks(1L));

        fillBlankQuestion("___ and ___", List.of(
                option(1, 1, "A", true), option(2, 1, "B", false),
                option(3, 2, "C", true), option(4, 2, "D", false)));
        assertEquals(2, scorer.computeQuestionMaxMarks(1L));
    }

    // ---------------------------------------------------------------
    // SAVED ROWS AND THE REVIEW
    // ---------------------------------------------------------------

    @Test
    void savedRowsRebuildTheReviewWithTheCorrectAnswers() {
        Question question = matchingQuestion();
        User user = new User();
        user.setUserId(5L);
        Exam exam = new Exam();
        exam.setExamId(9L);
        List<ExamQuestionAnswerDTO> submitted = List.of(submission(
                "MATCHING", match(10L, "Asset"), match(11L, "Asset")));

        ExamScoringService.Score score = scorer.score(List.of(1L), submitted);
        scorer.persistAnswerInfo(user, exam, null, submitted, score.questionScores());

        ArgumentCaptor<AnswerEvent> saved = ArgumentCaptor.forClass(AnswerEvent.class);
        verify(events, times(2)).save(saved.capture());
        AnswerEvent first = saved.getAllValues().get(0);
        assertEquals("MATCH", first.getArithmetic());
        assertEquals(1, first.getAnswerPosition());
        assertTrue(first.getIsCorrect());
        assertFalse(saved.getAllValues().get(1).getIsCorrect());
        assertEquals("EXAM_SUBMIT", first.getEventType());
        assertEquals(1.0, first.getMarks().doubleValue());

        saved.getAllValues().forEach(event -> event.setQuestion(question));
        when(events.findByUser_UserIdAndExam_ExamIdAndEventType(5L, 9L, "EXAM_SUBMIT"))
                .thenReturn(saved.getAllValues());

        ExamReviewQuestionDTO review = scorer.buildReviewFromAnswerInfo(5L, 9L, null).get(0);
        assertEquals("MATCHING", review.getQuestionType());
        assertFalse(review.getCorrect());
        assertEquals(2, review.getAnswers().size());
        assertEquals("correct", review.getAnswers().get(0).getAnsweredData().get("status"));
        assertEquals("Revenue", review.getCorrectMatches().get(11L));
        assertNull(review.getCorrectBlanks());
    }

    @Test
    void fillBlankRowsUseTheBlankNumberAsThePosition() {
        Question question = fillBlankQuestion("___ and ___", List.of(
                option(1, 1, "Debit", true), option(2, 2, "Credit", true)));
        User user = new User();
        MockExam mockExam = new MockExam();
        List<ExamQuestionAnswerDTO> submitted = List.of(submission(
                "FILL_IN_THE_BLANK", blank(2, "Credit")));

        scorer.persistAnswerInfo(user, null, mockExam, submitted,
                scorer.score(List.of(1L), submitted).questionScores());

        ArgumentCaptor<AnswerEvent> saved = ArgumentCaptor.forClass(AnswerEvent.class);
        verify(events).save(saved.capture());
        assertEquals("FILL_IN_THE_BLANK", saved.getValue().getArithmetic());
        assertEquals(2, saved.getValue().getAnswerPosition());
        assertTrue(saved.getValue().getIsCorrect());
        assertSame(mockExam, saved.getValue().getMockExam());
        assertSame(question, saved.getValue().getQuestion());
    }

    // ---------------------------------------------------------------
    // THE PAPER
    // ---------------------------------------------------------------

    @Test
    void thePaperHidesEveryMatchingAnswer() {
        Question question = new Question();
        List<QuestionMatchingPair> stored = matchingPairs(question);
        QuestionResponseDTO paper = new QuestionResponseDTO();
        paper.setPairs(new ArrayList<>(stored.stream().map(pair -> {
            MatchingPairResponseDTO dto = new MatchingPairResponseDTO();
            dto.setPairId(pair.getPairId());
            dto.setColumnA(pair.getColumnA());
            dto.setColumnB(pair.getColumnB());
            return dto;
        }).toList()));

        QuestionService.hideTextAnswers(paper, question, stored, List.of());

        assertTrue(paper.getPairs().stream().allMatch(pair -> pair.getColumnB() == null));
        assertEquals(3, paper.getMatchOptions().size());
        assertTrue(paper.getMatchOptions().containsAll(List.of("Asset", "Revenue", "Liability")));
    }

    @Test
    void aColumnBTextThatTwoPairsShareStaysTwiceOnThePaper() {
        Question question = new Question();
        List<QuestionMatchingPair> stored = List.of(
                pair(question, 10L, "Cash", "Asset", 1),
                pair(question, 11L, "Bank", "Asset", 2));
        QuestionResponseDTO paper = new QuestionResponseDTO();
        paper.setPairs(new ArrayList<>());

        QuestionService.hideTextAnswers(paper, question, stored, List.of());

        assertEquals(List.of("Asset", "Asset"), paper.getMatchOptions());
    }

    @Test
    void thePaperSendsOnlyABlankCountAndAWordBank() {
        Question question = new Question();
        question.setQuestionText("Cash is an ___ and a loan is a ___.");
        List<QuestionFillBlankAnswer> stored = List.of(
                option(1, 1, "Asset", true), option(2, 1, "Liability", false),
                option(3, 2, "Liability", true), option(4, 2, "Asset", false));
        QuestionResponseDTO paper = new QuestionResponseDTO();
        paper.setBlanks(List.of(new FillInTheBlankAnswerResponseDTO()));

        QuestionService.hideTextAnswers(paper, question, List.of(), stored);

        assertEquals(List.of(), paper.getBlanks());
        assertEquals(2, paper.getBlankCount());
        assertEquals(2, paper.getWordBank().size());
        assertTrue(paper.getWordBank().containsAll(List.of("Asset", "Liability")));
        assertNull(paper.getMatchOptions());
    }

    // ---------------------------------------------------------------
    // FIXTURES
    // ---------------------------------------------------------------

    private Question question(String type, String text) {
        QuestionType questionType = new QuestionType();
        questionType.setQuestionType(type);
        Question question = new Question();
        question.setQuestionId(1L);
        question.setQuestionType(questionType);
        question.setQuestionText(text);
        when(questions.findById(1L)).thenReturn(Optional.of(question));
        return question;
    }

    private Question matchingQuestion() {
        Question question = question("MATCH_THE_FOLLOWING", "Match the items.");
        when(pairs.findByQuestion_QuestionIdOrderByDisplayOrderAsc(1L)).thenReturn(matchingPairs(question));
        return question;
    }

    private List<QuestionMatchingPair> matchingPairs(Question question) {
        return List.of(pair(question, 10L, "Cash", "Asset", 1),
                pair(question, 11L, "Sales", "Revenue", 2),
                pair(question, 12L, "Loan", "Liability", 3));
    }

    private QuestionMatchingPair pair(Question question, Long id, String a, String b, int order) {
        QuestionMatchingPair pair = new QuestionMatchingPair();
        pair.setPairId(id);
        pair.setQuestion(question);
        pair.setColumnA(a);
        pair.setColumnB(b);
        pair.setDisplayOrder(order);
        return pair;
    }

    private Question fillBlankQuestion(String text, List<QuestionFillBlankAnswer> options) {
        Question question = question("FILL_IN_THE_BLANKS", text);
        when(blanks.findByQuestionQuestionIdOrderByDisplayOrderAsc(1L)).thenReturn(options);
        return question;
    }

    private QuestionFillBlankAnswer option(long id, Integer blankNumber, String text, boolean correct) {
        QuestionFillBlankAnswer option = new QuestionFillBlankAnswer();
        option.setAnswerId(id);
        option.setBlankNumber(blankNumber);
        option.setAnswerText(text);
        option.setIsCorrect(correct);
        option.setDisplayOrder((int) id);
        return option;
    }

    private ExamAnswerDTO match(Long pairId, String text) {
        return line(Map.of("pairId", pairId, "answerText", text));
    }

    private ExamAnswerDTO blank(int blankNumber, String text) {
        return line(Map.of("blankNumber", blankNumber, "answerText", text));
    }

    private ExamAnswerDTO line(Map<String, Object> data) {
        ExamAnswerDTO answer = new ExamAnswerDTO();
        answer.setAnsweredData(new HashMap<>(data));
        return answer;
    }

    private ExamQuestionAnswerDTO submission(String type, ExamAnswerDTO... answers) {
        ExamQuestionAnswerDTO submission = new ExamQuestionAnswerDTO();
        submission.setQuestionId(1L);
        submission.setQuestionType(type);
        submission.setAnswers(List.of(answers));
        return submission;
    }
}
