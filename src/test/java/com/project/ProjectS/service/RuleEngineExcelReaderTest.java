package com.project.ProjectS.service;

import com.project.ProjectS.mapper.RuleEngineExcelMapper;
import com.project.ProjectS.processor.RuleEngineExcelProcessor;
import com.project.ProjectS.repository.*;
import com.project.ProjectS.util.ExcelReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleEngineExcelReaderTest {
    @ParameterizedTest @ValueSource(booleans={true, false})
    void preservesActualColumnsAndPhysicalRowNumbersWhileSkippingBlankRows(boolean xlsx) throws Exception {
        try (Workbook workbook = xlsx ? new XSSFWorkbook() : new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rules");
            Row headers = sheet.createRow(0);
            headers.createCell(4).setCellValue(" Chapter ");
            headers.createCell(5).setCellValue("Attribute");
            // Both a physical blank header cell and an absent column must not shift later fields.
            headers.createCell(6);
            headers.createCell(8).setCellValue("Pair Order");
            headers.createCell(9).setCellValue("Field Type");
            Row first = sheet.createRow(1);
            first.createCell(4).setCellValue("Final Accounts");
            first.createCell(5).setCellValue("Commission is accrued");
            first.createCell(6).setCellValue("Ignored unheaded value");
            first.createCell(8).setCellValue(2);
            first.createCell(9).setCellValue("adjustments");
            sheet.createRow(2).createCell(4).setCellValue("   ");
            // Row 4 is absent; the following real row must still report Excel row 5.
            Row last = sheet.createRow(4);
            last.createCell(4).setCellValue("Journal");
            last.createCell(8).setCellValue(1);
            Row blank = sheet.createRow(5);
            Cell styled = blank.createCell(4);
            CellStyle style = workbook.createCellStyle(); style.setWrapText(true); styled.setCellStyle(style);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream(); workbook.write(bytes);
            var rows = new ExcelReader().readExcel(new MockMultipartFile("file", xlsx ? "rules.xlsx" : "rules.xls",
                    "application/octet-stream", bytes.toByteArray()));
            assertEquals(2, rows.size());
            assertEquals("Final Accounts", rows.get(0).get("chapter"));
            assertEquals("Commission is accrued", rows.get(0).get("attribute"));
            assertEquals("2", rows.get(0).get("pair_order"));
            assertEquals("adjustments", rows.get(0).get("field_type"));
            assertEquals("2", rows.get(0).get("_excel_row_number"));
            assertEquals("5", rows.get(1).get("_excel_row_number"));
            assertEquals("", rows.get(1).get("attribute"));
            assertFalse(rows.get(0).containsKey(""));
        }
    }

    @Test
    void stillReadsHeadersStartingAtColumnA() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("chapter");
            sheet.createRow(1).createCell(0).setCellValue("Journal");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(); workbook.write(bytes);
            var rows = new ExcelReader().readExcel(new MockMultipartFile("file", bytes.toByteArray()));
            assertEquals(List.of(Map.of("chapter", "Journal", "_excel_row_number", "2")), rows);
        }
    }

    @Test
    void processorIgnoresMetadataOnlyAndBlankRowsButRejectsIncompleteRealRows() {
        var mapper = mock(RuleEngineExcelMapper.class);
        var rules = mock(RuleEngineRepository.class);
        var chapters = mock(ChapterRepository.class);
        var attributes = mock(TableAttributeRepository.class);
        var headers = mock(TableHeaderRepository.class);
        var tables = mock(TableNameRepository.class);
        var processor = new RuleEngineExcelProcessor(mapper, rules, chapters, attributes, headers, tables);
        Map<String,String> blank = new HashMap<>();
        blank.put("_excel_row_number", "85"); blank.put("chapter", " "); blank.put("attribute", null);
        processor.process(List.of(blank, Map.of("_excel_row_number", "86")));
        verifyNoInteractions(mapper, rules, chapters, attributes, headers, tables);
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> processor.process(List.of(Map.of("_excel_row_number", "87", "attribute", "Rent"))));
        assertEquals("chapter is required", error.getMessage());
        verify(rules, never()).save(any());
    }
}
