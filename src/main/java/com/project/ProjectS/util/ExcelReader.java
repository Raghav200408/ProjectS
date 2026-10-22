package com.project.ProjectS.util;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class ExcelReader {

    public List<Map<String, String>> readExcel(
            MultipartFile file) throws IOException {

        List<Map<String, String>> excelData =
                new ArrayList<>();

        try (Workbook workbook =
                     WorkbookFactory.create(
                             file.getInputStream())) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            if (sheet == null) {
                return excelData;
            }

            Row headerRow =
                    sheet.getRow(0);

            if (headerRow == null) {
                return excelData;
            }

            Map<Integer, String> headers = new LinkedHashMap<>();

            // =================================================
            // READ HEADER ROW
            // =================================================

            for (Cell cell : headerRow) {

                String header =
                        getCellValue(cell)
                                .trim()
                                .toLowerCase(Locale.ROOT)
                                .replace(" ", "_");

                if (!header.isBlank()) {
                    headers.put(cell.getColumnIndex(), header);
                }
            }

            if (headers.isEmpty()) {
                return excelData;
            }

            System.out.println(
                    "Excel Headers = "
                            + headers
            );

            // =================================================
            // READ DATA ROWS
            // =================================================

            for (int i = 1;
                 i <= sheet.getLastRowNum();
                 i++) {

                Row row =
                        sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Map<String, String> rowData =
                        new LinkedHashMap<>();
                for (Map.Entry<Integer, String> header : headers.entrySet()) {

                    Cell cell =
                            row.getCell(
                                    header.getKey(),
                                    Row.MissingCellPolicy
                                            .RETURN_BLANK_AS_NULL
                            );

                    rowData.put(
                            header.getValue(),
                            getCellValue(cell)
                    );
                }

                if (rowData.values().stream().allMatch(String::isBlank)) {
                    continue;
                }
                rowData.put("_excel_row_number", String.valueOf(i + 1));
                excelData.add(rowData);
            }
        }

        return excelData;
    }

    // =========================================================
    // GET CELL VALUE
    // =========================================================

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {

            case STRING -> {

                String value =
                        cell.getStringCellValue();

                yield value == null
                        ? ""
                        : value.trim();
            }

            case NUMERIC -> {

                if (DateUtil.isCellDateFormatted(cell)) {

                    yield cell
                            .getLocalDateTimeCellValue()
                            .toLocalDate()
                            .toString();
                }

                double value =
                        cell.getNumericCellValue();

                if (value == (long) value) {

                    yield String.valueOf(
                            (long) value
                    );
                }

                yield String.valueOf(value);
            }

            case BOOLEAN ->
                    String.valueOf(
                            cell.getBooleanCellValue()
                    );

            case FORMULA -> {

                try {

                    FormulaEvaluator evaluator =
                            cell.getSheet()
                                    .getWorkbook()
                                    .getCreationHelper()
                                    .createFormulaEvaluator();

                    CellValue cellValue =
                            evaluator.evaluate(cell);

                    if (cellValue == null) {
                        yield "";
                    }

                    yield switch (
                            cellValue.getCellType()) {

                        case STRING ->
                                cellValue
                                        .getStringValue()
                                        .trim();

                        case NUMERIC -> {

                            double value =
                                    cellValue
                                            .getNumberValue();

                            if (value ==
                                    (long) value) {

                                yield String.valueOf(
                                        (long) value
                                );
                            }

                            yield String.valueOf(
                                    value
                            );
                        }

                        case BOOLEAN ->
                                String.valueOf(
                                        cellValue
                                                .getBooleanValue()
                                );

                        default -> "";
                    };

                } catch (Exception e) {

                    yield "";
                }
            }

            case BLANK -> "";

            default -> "";
        };
    }
}
