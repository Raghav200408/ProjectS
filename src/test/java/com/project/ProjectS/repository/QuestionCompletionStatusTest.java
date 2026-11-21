package com.project.ProjectS.repository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuestionCompletionStatusTest {
    @Test
    void completionRequiresEveryRequiredAnswerAndEmptyQuestionsAreNotSolved() {
        assertEquals("ANSWERED", new PracticePerformanceRepository.QuestionAnswerStatus(1, 4,4,true).status());
        assertEquals("PARTIAL", new PracticePerformanceRepository.QuestionAnswerStatus(1, 4, 3,true).status());
        assertEquals("UNANSWERED", new PracticePerformanceRepository.QuestionAnswerStatus(1, 4, 0,true).status());
        assertEquals("UNANSWERED", new PracticePerformanceRepository.QuestionAnswerStatus(1, 0, 0,true).status());
    }

    @Test
    void batchStatusesRequireCorrectAnswersAndUseAuthenticatedUserScope() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var repository = new PracticePerformanceRepository(jdbc);
        repository.findQuestionAnswerStatuses(27L, 5L);
        var parameters = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).query(anyString(), parameters.capture(),
                org.mockito.ArgumentMatchers.<RowMapper<PracticePerformanceRepository.QuestionAnswerStatus>>any());
        assertEquals(true, parameters.getValue().getValue("requireCorrect"));
        assertEquals(27L, parameters.getValue().getValue("userId"));
        assertEquals(5L, parameters.getValue().getValue("topicId"));
    }
}
