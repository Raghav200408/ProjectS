package com.project.ProjectS.processor;

import java.util.List;
import java.util.Map;

public interface ExcelProcessor {

    /**
     * Process Excel data after it has been read.
     *
     * @param excelData List of rows where each row is a Map<ColumnHeader, CellValue>
     */
    void process(List<Map<String, String>> excelData);

}