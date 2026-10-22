package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineExcelMapper;
import com.project.ProjectS.mapper.RuleEngineMapper;
import com.project.ProjectS.model.RuleEngineRequestDTO;
import com.project.ProjectS.processor.RuleEngineExcelProcessor;
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
        when(rules.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(rules.findById(1L)).thenReturn(Optional.of(new RuleEngine()));
    }

    void save(String path, String fieldType) {
        if (path.equals("upload")) {
            Map<String,String> row = new HashMap<>();
            row.put("chapter", "Chapter"); row.put("attribute", "Rent");
            row.put("pair_attribute", "Rent payable"); row.put("_excel_row_number", "17");
            row.put("field_type", fieldType);
            new RuleEngineExcelProcessor(new RuleEngineExcelMapper(), rules, chapters, attributes, headers, tables)
                    .process(List.of(row));
        } else {
            RuleEngineRequestDTO request = new RuleEngineRequestDTO();
            request.setChapterName("Chapter"); request.setAttributeName("Rent");
            request.setPairAttributeName("Rent payable"); request.setFieldType(fieldType);
            RuleEngineService service = new RuleEngineService(rules, chapters, attributes, tables, headers,
                    mock(RuleEngineMapper.class));
            if (path.equals("create")) service.create(request); else service.update(1L, request);
        }
    }

    @ParameterizedTest @ValueSource(strings={"create", "update", "upload"})
    void resolvesBothAttributesWithinSharedHeader(String path) {
        save(path, "adjustments");
        ArgumentCaptor<RuleEngine> captor = ArgumentCaptor.forClass(RuleEngine.class);
        verify(rules).save(captor.capture());
        assertSame(header, captor.getValue().getFieldType());
        assertSame(attribute, captor.getValue().getTableAttributeid());
        assertSame(pair, captor.getValue().getPairAttribute());
        verify(attributes, never()).findByName(anyString());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update", "upload"})
    void rejectsMissingFieldType(String path) {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> save(path, " "))
                .getMessage().toLowerCase().contains(path.equals("upload") ? "field_type" : "field type"));
        verify(rules, never()).save(any());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update", "upload"})
    void rejectsAttributeFromAnotherHeader(String path) {
        when(attributes.findByNameAndTableHeader("Rent", header)).thenReturn(Optional.empty());
        String error = assertThrows(IllegalArgumentException.class, () -> save(path, "adjustments")).getMessage();
        assertTrue(error.contains("Rent")); assertTrue(error.contains("adjustments"));
        if (path.equals("upload")) assertTrue(error.contains("Excel row 17"));
        verify(rules, never()).save(any());
    }

    @ParameterizedTest @ValueSource(strings={"create", "update", "upload"})
    void reportsDuplicatePairWithinSameHeader(String path) {
        when(attributes.findByNameAndTableHeader("Rent payable", header))
                .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));
        String error = assertThrows(IllegalArgumentException.class, () -> save(path, "adjustments")).getMessage();
        assertTrue(error.contains("Rent payable")); assertTrue(error.contains("adjustments"));
        assertTrue(error.contains("multiple"));
        if (path.equals("upload")) assertTrue(error.contains("Excel row 17: pair_attribute"));
        verify(rules, never()).save(any());
    }
}
