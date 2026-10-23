package com.project.ProjectS.service;

import com.project.ProjectS.controller.RuleEngineController;
import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineMapper;
import com.project.ProjectS.exception.DuplicateRuleEngineException;
import com.project.ProjectS.model.RuleEngineRequestDTO;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleEngineUniquenessTest {
    final RuleEngineRepository rules = mock(RuleEngineRepository.class);
    final ChapterRepository chapters = mock(ChapterRepository.class);
    final TableAttributeRepository attributes = mock(TableAttributeRepository.class);
    final Chapter chapter = new Chapter();
    final TableAttribute main = new TableAttribute();
    final TableAttribute pair = new TableAttribute();
    final RuleEngine existing = new RuleEngine();
    final RuleEngineService service = new RuleEngineService(rules, chapters, attributes,
            mock(TableNameRepository.class), mock(TableHeaderRepository.class), mock(RuleEngineMapper.class));

    RuleEngineUniquenessTest() {
        chapter.setChapterId(2L); chapter.setName("Chapter");
        main.setAttributeId(10L); main.setName("Rent"); main.setTableHeader(new TableHeader());
        pair.setAttributeId(11L); pair.setName("Cash"); pair.setTableHeader(new TableHeader());
        existing.setRuleEngineId(7L); existing.setChapter(chapter); existing.setTableAttributeid(main);
        when(chapters.findByName("Chapter")).thenReturn(Optional.of(chapter));
        when(attributes.findById(10L)).thenReturn(Optional.of(main));
        when(attributes.findById(11L)).thenReturn(Optional.of(pair));
        when(rules.findById(7L)).thenReturn(Optional.of(existing));
    }
    RuleEngineRequestDTO request() {
        RuleEngineRequestDTO request = new RuleEngineRequestDTO(); request.setChapterName("Chapter");
        request.setAttributeId(10L); request.setPairAttributeId(11L); return request;
    }

    @Test void createRejectsExistingCombinationWithoutSavingOrChangingStatus() {
        when(rules.existsForChapterAndAttribute(2L, 10L, null)).thenReturn(true);
        var error = assertThrows(DuplicateRuleEngineException.class, () -> service.create(request()));
        assertTrue(error.getMessage().contains("Rent")); assertTrue(error.getMessage().contains("Chapter"));
        assertEquals("DRAFT", main.getRowStatus());
        verify(rules, never()).saveAndFlush(any()); verify(attributes, never()).save(any());
        var response = new RuleEngineController(service, null, null).duplicateRule(error);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(error.getMessage(), response.getBody().get("message"));
    }

    @Test void updatingSameRuleExcludesItsOwnId() {
        service.update(7L, request());
        verify(rules).existsForChapterAndAttribute(2L, 10L, 7L);
        verify(rules).saveAndFlush(existing);
    }

    @Test void updateToAnotherRulesCombinationIsRejectedBeforeMutatingExistingRule() {
        Chapter originalChapter = new Chapter(); originalChapter.setChapterId(99L);
        existing.setChapter(originalChapter);
        when(rules.existsForChapterAndAttribute(2L, 10L, 7L)).thenReturn(true);
        assertThrows(DuplicateRuleEngineException.class, () -> service.update(7L, request()));
        assertSame(originalChapter, existing.getChapter());
        verify(rules, never()).saveAndFlush(any());
    }

    @Test void uniqueConstraintRaceReturnsReadableDuplicateError() {
        when(rules.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException(
                "duplicate key violates unique constraint rule_engines_chapter_attribute_key"));
        assertThrows(DuplicateRuleEngineException.class, () -> service.create(request()));
        verify(attributes, never()).save(any());
    }

    @Test void otherDatabaseErrorsAreNotMisreportedAsDuplicates() {
        var failure = new DataIntegrityViolationException("some_other_constraint");
        when(rules.saveAndFlush(any())).thenThrow(failure);
        assertSame(failure, assertThrows(DataIntegrityViolationException.class, () -> service.create(request())));
    }
}
