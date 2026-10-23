package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineExcelMapper;
import com.project.ProjectS.model.*;
import com.project.ProjectS.processor.RuleEngineExcelProcessor;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleEngineUploadTest {
    final RuleEngineRepository rules = mock(RuleEngineRepository.class);
    final TableAttributeRepository attributes = mock(TableAttributeRepository.class);
    final TableHeaderRepository headers = mock(TableHeaderRepository.class);
    final ChapterRepository chapters = mock(ChapterRepository.class);
    final TableNameRepository tables = mock(TableNameRepository.class);
    final TableHeader debit = header(1L, "Debit");
    final TableHeader credit = header(2L, "Credit");
    final Map<String, TableAttribute> stored = new HashMap<>();
    final Set<String> savedRules = new HashSet<>();
    final CountingTransactions transactions = new CountingTransactions();
    final RuleEngineExcelUploadService upload;

    RuleEngineUploadTest() {
        Chapter chapter = new Chapter(); chapter.setChapterId(1L); chapter.setName("Chapter");
        when(chapters.findByName("Chapter")).thenReturn(Optional.of(chapter));
        when(headers.findByName("Debit")).thenReturn(Optional.of(debit));
        when(headers.findByName("Credit")).thenReturn(Optional.of(credit));
        when(attributes.findByNameAndTableHeader(anyString(), any())).thenAnswer(call ->
                Optional.ofNullable(stored.get(key(call.getArgument(0), call.getArgument(1)))));
        when(attributes.saveAndFlush(any())).thenAnswer(call -> {
            TableAttribute a = call.getArgument(0); a.setAttributeId((long) stored.size() + 1);
            stored.put(key(a.getName(), a.getTableHeader()), a); return a;
        });
        when(rules.existsForChapterAndAttribute(any(), any(), any())).thenAnswer(call ->
                savedRules.contains(call.getArgument(0) + "|" + call.getArgument(1)));
        when(rules.saveAndFlush(any())).thenAnswer(call -> {
            RuleEngine rule = call.getArgument(0);
            savedRules.add(rule.getChapter().getChapterId() + "|" + rule.getTableAttributeid().getAttributeId());
            return rule;
        });
        RuleEngineExcelProcessor processor = new RuleEngineExcelProcessor(new RuleEngineExcelMapper(),
                rules, chapters, attributes, headers, tables);
        ProxyFactory proxy = new ProxyFactory(processor);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
        upload = new RuleEngineExcelUploadService((RuleEngineExcelProcessor) proxy.getProxy());
    }

    static TableHeader header(Long id, String name) {
        TableHeader h = new TableHeader(); h.setHeaderId(id); h.setName(name); return h;
    }
    static String key(String name, TableHeader header) { return name + "|" + header.getHeaderId(); }
    TableAttribute existing(String name, TableHeader header) {
        TableAttribute a = new TableAttribute(); a.setName(name); a.setTableHeader(header);
        a.setAttributeId((long) stored.size() + 1);
        stored.put(key(name, header), a); return a;
    }
    Map<String,String> row(int number) {
        return new HashMap<>(Map.of("_excel_row_number", "" + number, "chapter", "Chapter",
                "attribute", "Sales returns", "field_type", "Debit", "pair_attribute", "Sales",
                "paired_field_type", "Credit", "pair_order", "2"));
    }

    @Test void retriesCreateMainThenPairThenRuleUnderTheirRespectiveHeaders() {
        var main = upload.process(List.of(row(223)));
        assertEquals(1, main.getCreatedAttributes()); assertEquals(1, main.getSkippedRows());
        assertEquals("attribute", main.getErrors().get(0).field());
        assertEquals(223, main.getErrors().get(0).rowNumber());
        assertTrue(main.getErrors().get(0).errorMessage().contains("Created in Table Attributes as DRAFT"));
        assertEquals("DRAFT", stored.get(key("Sales returns", debit)).getRowStatus());
        verify(headers, never()).findByName("Credit");
        verify(attributes, never()).findByNameAndTableHeader("Sales", credit);
        verify(rules, never()).saveAndFlush(any());

        var pair = upload.process(List.of(row(223)));
        assertEquals(1, pair.getCreatedAttributes());
        assertEquals("pair_attribute", pair.getErrors().get(0).field());
        assertEquals("Credit", pair.getErrors().get(0).headerName());
        assertEquals("DRAFT", stored.get(key("Sales", credit)).getRowStatus());
        assertEquals("DRAFT", stored.get(key("Sales returns", debit)).getRowStatus());

        var result = upload.process(List.of(row(223)));
        assertTrue(result.isSuccess()); assertEquals(1, result.getRulesUploaded());
        assertEquals(0, result.getCreatedAttributes()); assertEquals(2, stored.size());
        ArgumentCaptor<RuleEngine> captor = ArgumentCaptor.forClass(RuleEngine.class);
        verify(rules).saveAndFlush(captor.capture());
        assertSame(debit, captor.getValue().getFieldType());
        assertSame(credit, captor.getValue().getPairedFieldType());
        assertEquals("RULE", stored.get(key("Sales returns", debit)).getRowStatus());
        assertEquals("DRAFT", stored.get(key("Sales", credit)).getRowStatus());
        verify(attributes, never()).save(stored.get(key("Sales", credit)));
        assertEquals(3, transactions.committed); assertEquals(0, transactions.rolledBack);
    }

    @Test void identicalNameAndHeaderCreatesOneRecordAcrossRetriesAndRows() {
        var row = row(8); row.put("pair_attribute", "Sales returns"); row.put("paired_field_type", "Debit");
        var first = upload.process(List.of(row));
        assertEquals(1, first.getCreatedAttributes());
        var retry = upload.process(List.of(row, new HashMap<>(row)));
        assertEquals(1, retry.getRulesUploaded()); assertEquals(1, retry.getSkippedRows());
        assertEquals(0, retry.getCreatedAttributes());
        assertEquals(1, stored.size()); verify(attributes, times(1)).saveAndFlush(any());
        ArgumentCaptor<RuleEngine> captor = ArgumentCaptor.forClass(RuleEngine.class);
        verify(rules).saveAndFlush(captor.capture());
        assertSame(captor.getValue().getTableAttributeid(), captor.getValue().getPairAttribute());
    }

    @Test void sameNameDifferentHeadersCreatesTwoDistinctAttributes() {
        var row = row(3); row.put("pair_attribute", "Sales returns");
        assertEquals(1, upload.process(List.of(row)).getCreatedAttributes());
        assertEquals(1, upload.process(List.of(row)).getCreatedAttributes());
        assertEquals(1, upload.process(List.of(row)).getRulesUploaded());
        assertEquals(2, stored.size());
    }

    @Test void unknownMainHeaderSkipsWithoutCreatingAndContinuesValidRows() {
        existing("Sales returns", debit); existing("Sales", credit);
        var bad = row(7); bad.put("field_type", "Unknown");
        var result = upload.process(List.of(bad, row(12)));
        assertEquals(1, result.getRulesUploaded()); assertEquals(1, result.getSkippedRows());
        assertEquals(7, result.getErrors().get(0).rowNumber());
        assertEquals("field_type", result.getErrors().get(0).field());
        verify(attributes, never()).saveAndFlush(any());
        assertEquals(1, transactions.rolledBack); assertEquals(1, transactions.committed);
    }

    @Test void missingPairedHeaderDoesNotCreatePairOrFallbackToMainHeader() {
        existing("Sales returns", debit);
        var row = row(9); row.remove("paired_field_type");
        var result = upload.process(List.of(row));
        assertEquals("paired_field_type", result.getErrors().get(0).field());
        verify(attributes, never()).saveAndFlush(any());
        verify(attributes, never()).findByNameAndTableHeader("Sales", debit);
    }

    @Test void mainCreationDoesNotRequirePairHeaderUntilNextAttempt() {
        var row = row(15); row.remove("paired_field_type");
        var first = upload.process(List.of(row));
        assertEquals(1, first.getCreatedAttributes()); assertEquals("attribute", first.getErrors().get(0).field());
        var second = upload.process(List.of(row));
        assertEquals(0, second.getCreatedAttributes());
        assertEquals("paired_field_type", second.getErrors().get(0).field());
    }

    @Test void ambiguousMainIsReportedWithoutCreatingOrCheckingPair() {
        when(attributes.findByNameAndTableHeader("Sales returns", debit))
                .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));
        var result = upload.process(List.of(row(25)));
        assertEquals("attribute", result.getErrors().get(0).field());
        assertTrue(result.getErrors().get(0).errorMessage().contains("multiple"));
        verify(attributes, never()).saveAndFlush(any());
        verify(headers, never()).findByName("Credit");
    }

    @Test void ambiguousPairIsReportedWithoutCreating() {
        existing("Sales returns", debit);
        when(attributes.findByNameAndTableHeader("Sales", credit))
                .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));
        var result = upload.process(List.of(row(26)));
        assertEquals("pair_attribute", result.getErrors().get(0).field());
        verify(attributes, never()).saveAndFlush(any());
    }

    @Test void failedRuleSaveDoesNotAbortOtherRowsOrLoseCreatedDraftOutcome() {
        existing("Sales returns", debit); existing("Sales", credit);
        var draft = row(2); draft.put("attribute", "Missing main");
        doThrow(new IllegalStateException("Save rejected"))
                .doAnswer(call -> call.getArgument(0)).when(rules).saveAndFlush(any());
        var result = upload.process(List.of(draft, row(17), row(20)));
        assertEquals(3, result.getTotalRows()); assertEquals(1, result.getRulesUploaded());
        assertEquals(2, result.getSkippedRows()); assertEquals(1, result.getCreatedAttributes());
        assertEquals(List.of(2, 17), result.getErrors().stream().map(RuleEngineUploadErrorDTO::rowNumber).toList());
        assertEquals(2, transactions.committed); assertEquals(1, transactions.rolledBack);
    }

    @Test void existingRuleSkipsBeforeCheckingOrCreatingPairAndKeepsExcelRowNumber() {
        TableAttribute main = existing("Sales returns", debit);
        savedRules.add("1|" + main.getAttributeId());
        var result = upload.process(List.of(row(223)));
        assertEquals(0, result.getRulesUploaded()); assertEquals(1, result.getSkippedRows());
        assertEquals(0, result.getCreatedAttributes());
        assertEquals(223, result.getErrors().get(0).rowNumber());
        assertEquals("attribute", result.getErrors().get(0).field());
        assertTrue(result.getErrors().get(0).errorMessage().contains("already exists"));
        assertEquals("DRAFT", main.getRowStatus());
        verify(headers, never()).findByName("Credit");
        verify(attributes, never()).saveAndFlush(any());
        verify(attributes, never()).save(any());
        verify(rules, never()).saveAndFlush(any());
    }

    @Test void duplicateInSameFileSkipsButOtherChapterAndAttributeAreAllowed() {
        existing("Sales returns", debit); existing("Sales", credit); existing("Purchases", debit);
        Chapter other = new Chapter(); other.setChapterId(2L); other.setName("Other chapter");
        when(chapters.findByName("Other chapter")).thenReturn(Optional.of(other));
        var anotherChapter = row(12); anotherChapter.put("chapter", "Other chapter");
        var anotherAttribute = row(14); anotherAttribute.put("attribute", "Purchases");
        var result = upload.process(List.of(row(2), row(9), anotherChapter, anotherAttribute));
        assertEquals(3, result.getRulesUploaded()); assertEquals(1, result.getSkippedRows());
        assertEquals(9, result.getErrors().get(0).rowNumber());
    }

    @Test void concurrentConstraintViolationIsReportedAsDuplicateAndLaterRowsContinue() {
        existing("Sales returns", debit); existing("Sales", credit);
        doThrow(new org.springframework.dao.DataIntegrityViolationException(
                "duplicate key violates unique constraint rule_engines_chapter_attribute_key"))
                .doAnswer(call -> call.getArgument(0)).when(rules).saveAndFlush(any());
        var result = upload.process(List.of(row(8), row(20)));
        assertEquals(1, result.getRulesUploaded()); assertEquals(1, result.getSkippedRows());
        assertTrue(result.getErrors().get(0).errorMessage().contains("already exists"));
        assertFalse(result.getErrors().get(0).errorMessage().contains("duplicate key"));
        assertEquals(1, transactions.rolledBack);
    }

    @Test void identicalAttributeNamesUnderDifferentHeadersRemainDistinctRuleKeys() {
        existing("Sales returns", debit); existing("Sales", credit); existing("Sales returns", credit);
        var creditMain = row(30); creditMain.put("field_type", "Credit");
        var result = upload.process(List.of(row(2), creditMain));
        assertEquals(2, result.getRulesUploaded()); assertEquals(0, result.getSkippedRows());
    }

    static class CountingTransactions extends AbstractPlatformTransactionManager {
        int committed, rolledBack;
        protected Object doGetTransaction() { return new Object(); }
        protected void doBegin(Object transaction, TransactionDefinition definition) {}
        protected void doCommit(DefaultTransactionStatus status) { committed++; }
        protected void doRollback(DefaultTransactionStatus status) { rolledBack++; }
    }
}
