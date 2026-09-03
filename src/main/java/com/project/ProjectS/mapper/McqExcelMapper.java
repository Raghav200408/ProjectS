package com.project.ProjectS.mapper;

import com.project.ProjectS.model.McqOptionDTO;
import com.project.ProjectS.model.McqQuestionRequestDTO;

import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.List;

public class McqExcelMapper {

    private McqExcelMapper() {
        // Utility class
    }

    public static McqQuestionRequestDTO mapRow(
            Row row,
            int rowNumber
    ) {

        McqQuestionRequestDTO request =
                new McqQuestionRequestDTO();

        request.setCourseId(
                getLongValue(row, 0, "Course ID", rowNumber)
        );

        request.setChapterId(
                getLongValue(row, 1, "Chapter ID", rowNumber)
        );

        request.setCategoryId(
                getLongValue(row, 2, "Category ID", rowNumber)
        );

        request.setQuestionText(
                getStringValue(row, 3, "Question", rowNumber)
        );


        List<McqOptionDTO> options =
                new ArrayList<>();

        for (int i = 0; i < 4; i++) {

            int columnIndex = 4 + i;

            String optionText =
                    getStringValue(
                            row,
                            columnIndex,
                            "Option " + (i + 1),
                            rowNumber
                    );

            McqOptionDTO option =
                    new McqOptionDTO();

            option.setOptionOrder(i + 1);

            option.setOptionText(optionText);

            option.setIsCorrect(false);

            options.add(option);
        }


        int correctAnswer =
                getIntValue(
                        row,
                        8,
                        "Correct Answer",
                        rowNumber
                );

        if (correctAnswer < 1 || correctAnswer > 4) {

            throw new RuntimeException(
                    "Invalid correct answer at Excel row "
                            + rowNumber
                            + ". Expected 1, 2, 3 or 4."
            );
        }

        options.get(correctAnswer - 1)
                .setIsCorrect(true);

        request.setOptions(options);

        String questionType =
                getStringValue(
                        row,
                        9,
                        "Question Type",
                        rowNumber
                );

        request.setQuestionType(questionType);

        double marks =
                getDoubleValue(
                        row,
                        10,
                        "Marks",
                        rowNumber
                );

        request.setMarks(marks);

        return request;
    }

    private static String getStringValue(
            Row row,
            int columnIndex,
            String columnName,
            int rowNumber
    ) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {

            throw new RuntimeException(
                    columnName
                            + " is missing at Excel row "
                            + rowNumber
            );
        }

        String value;

        if (cell.getCellType() == CellType.STRING) {

            value = cell.getStringCellValue();

        } else if (
                cell.getCellType() == CellType.NUMERIC
        ) {

            value =
                    String.valueOf(
                            cell.getNumericCellValue()
                    );

        } else {

            value =
                    cell.toString();
        }

        if (value == null ||
                value.trim().isEmpty()) {

            throw new RuntimeException(
                    columnName
                            + " cannot be empty at Excel row "
                            + rowNumber
            );
        }

        return value.trim();
    }

    private static Long getLongValue(
            Row row,
            int columnIndex,
            String columnName,
            int rowNumber
    ) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {

            throw new RuntimeException(
                    columnName
                            + " is missing at Excel row "
                            + rowNumber
            );
        }

        try {

            if (cell.getCellType() ==
                    CellType.NUMERIC) {

                return (long)
                        cell.getNumericCellValue();
            }

            if (cell.getCellType() ==
                    CellType.STRING) {

                return Long.parseLong(
                        cell.getStringCellValue()
                                .trim()
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid "
                            + columnName
                            + " at Excel row "
                            + rowNumber
            );
        }

        throw new RuntimeException(
                "Invalid "
                        + columnName
                        + " at Excel row "
                        + rowNumber
        );
    }

    private static int getIntValue(
            Row row,
            int columnIndex,
            String columnName,
            int rowNumber
    ) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {

            throw new RuntimeException(
                    columnName
                            + " is missing at Excel row "
                            + rowNumber
            );
        }

        try {

            if (cell.getCellType() ==
                    CellType.NUMERIC) {

                return (int)
                        cell.getNumericCellValue();
            }

            if (cell.getCellType() ==
                    CellType.STRING) {

                return Integer.parseInt(
                        cell.getStringCellValue()
                                .trim()
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid "
                            + columnName
                            + " at Excel row "
                            + rowNumber
            );
        }

        throw new RuntimeException(
                "Invalid "
                        + columnName
                        + " at Excel row "
                        + rowNumber
        );
    }

    private static double getDoubleValue(
            Row row,
            int columnIndex,
            String columnName,
            int rowNumber
    ) {

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {

            throw new RuntimeException(
                    columnName
                            + " is missing at Excel row "
                            + rowNumber
            );
        }

        try {

            if (cell.getCellType() ==
                    CellType.NUMERIC) {

                return cell.getNumericCellValue();
            }

            if (cell.getCellType() ==
                    CellType.STRING) {

                return Double.parseDouble(
                        cell.getStringCellValue()
                                .trim()
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid "
                            + columnName
                            + " at Excel row "
                            + rowNumber
            );
        }

        throw new RuntimeException(
                "Invalid "
                        + columnName
                        + " at Excel row "
                        + rowNumber
        );
    }
}