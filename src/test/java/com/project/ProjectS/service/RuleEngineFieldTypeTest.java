package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineMapper;
import com.project.ProjectS.model.RuleEngineRequestDTO;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleEngineFieldTypeTest {
    final RuleEngineRepository rules = mock(RuleEngineRepository.class);
    final ChapterRepository chapters = mock(ChapterRepository.class);
    final TableAttributeRepository attributes = mock(TableAttributeRepository.class);
    final TableHeaderRepository headers = mock(TableHeaderRepository.class);
    final TableNameRepository tables = mock(TableNameRepository.class);
    final TableHeader header = new TableHeader();
    final TableAttribute attribute = new TableAttribute();
    final TableAttribute pair = new TableAttribute();

    RuleEngineFieldTypeTest() {
        header.setHeaderId(4L); header.setName("adjustments");
        attribute.setTableHeader(header); pair.setTableHeader(header);
        when(chapters.findByName("Chapter")).thenReturn(Optional.of(new Chapter()));
        when(headers.findByName("adjustments")).thenReturn(Optional.of(header));
        when(attributes.findByNameAndTableHeader("Rent", header)).thenReturn(Optional.of(attribute));
        when(attributes.findByNameAndTableHeader("Rent payable", header)).thenReturn(Optional.of(pair));
        // Name alone is ambiguous across headers; none of the paths may use it.
        when(attributes.findByName(anyString())).thenThrow(new IncorrectResultSizeDataAccessException(1, 2));
        when(rules.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(rules.findById(1L)).thenReturn(Optional.of(new RuleEngine()));
    }

    void save(String path, String fieldType) {
            RuleEngineRequestDTO request = new RuleEngineRequestDTO();
            request.setChapterName("Chapter"); request.setAttributeName("Rent");
            request.setPairAttributeName("Rent payable"); request.setFieldType(fieldType);
            request.setPairedFieldType(fieldType);
            RuleEngineService service = new RuleEngineService(rules, chapters, attributes, tables, headers,
                    mock(RuleEngineMapper.class));
            if (path.equals("create")) service.create(request); else service.update(1L, request);
    }

    @ParameterizedTest @ValueSource(strings={"create", "update"})
    void resolvesBothAttributesWithinSharedHeader(String path) {
        save(path, "adjustments");
        ArgumentCaptor<RuleEngine> captor = ArgumentCaptor.forClass(RuleEngine.class);
        verify(rules).saveAndFlush(captor.capture());
        assertSame(header, captor.getValue().getFieldType());
        assertSame(attribute, captor.getValue().getTableAttributeid());
        assertSame(pair, captor.getValue().getPairAttribute());
        verify(attributes, never()).findByName(anyString());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update"})
    void rejectsMissingFieldType(String path) {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> save(path, " "))
                .getMessage().toLowerCase().contains(path.equals("upload") ? "field_type" : "field type"));
        verify(rules, never()).saveAndFlush(any());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update"})
    void rejectsAttributeFromAnotherHeader(String path) {
        when(attributes.findByNameAndTableHeader("Rent", header)).thenReturn(Optional.empty());
        String error = assertThrows(IllegalArgumentException.class, () -> save(path, "adjustments")).getMessage();
        assertTrue(error.contains("Rent")); assertTrue(error.contains("adjustments"));
        if (path.equals("upload")) assertTrue(error.contains("Excel row 17"));
        verify(rules, never()).saveAndFlush(any());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update"})
    void reportsDuplicatePairWithinSameHeader(String path) {
        when(attributes.findByNameAndTableHeader("Rent payable", header))
                .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));
        String error = assertThrows(IllegalArgumentException.class, () -> save(path, "adjustments")).getMessage();
        assertTrue(error.contains("Rent payable")); assertTrue(error.contains("adjustments"));
        assertTrue(error.contains("multiple"));
        if (path.equals("upload")) assertTrue(error.contains("Excel row 17: pair_attribute"));
        verify(rules, never()).saveAndFlush(any());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update"})
    void formIdsAllowDifferentHeadersAndMissingFieldType(String path) {
        TableHeader otherHeader = new TableHeader(); otherHeader.setName("Debit");
        attribute.setTableHeader(otherHeader);
        when(attributes.findById(10L)).thenReturn(Optional.of(attribute));
        when(attributes.findById(30L)).thenReturn(Optional.of(pair));
        RuleEngineRequestDTO request = new RuleEngineRequestDTO();
        request.setChapterName("Chapter"); request.setAttributeId(10L); request.setPairAttributeId(30L);
        request.setAttributeName("Rent"); request.setPairAttributeName("Rent payable");
        RuleEngineService service = new RuleEngineService(rules, chapters, attributes, tables, headers,
                mock(RuleEngineMapper.class));
        if (path.equals("create")) service.create(request); else service.update(1L, request);
        ArgumentCaptor<RuleEngine> captor = ArgumentCaptor.forClass(RuleEngine.class);
        verify(rules).saveAndFlush(captor.capture());
        assertSame(otherHeader, captor.getValue().getFieldType());
        assertSame(header, captor.getValue().getPairedFieldType());
        assertSame(attribute, captor.getValue().getTableAttributeid());
        assertSame(pair, captor.getValue().getPairAttribute());
        verify(attributes, never()).findByName(anyString());
        verify(attributes, never()).findByNameAndTableHeader(anyString(), any());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update"})
    void formRejectsUnknownAttributeId(String path) {
        when(attributes.findById(10L)).thenReturn(Optional.of(attribute));
        RuleEngineRequestDTO request = new RuleEngineRequestDTO();
        request.setChapterName("Chapter"); request.setAttributeId(10L); request.setPairAttributeId(999L);
        RuleEngineService service = new RuleEngineService(rules, chapters, attributes, tables, headers,
                mock(RuleEngineMapper.class));
        String error = assertThrows(IllegalArgumentException.class, () -> {
            if (path.equals("create")) service.create(request); else service.update(1L, request);
        }).getMessage();
        assertTrue(error.contains("Pair Attribute ID 999 not found"));
        verify(rules, never()).saveAndFlush(any());
    }
}
