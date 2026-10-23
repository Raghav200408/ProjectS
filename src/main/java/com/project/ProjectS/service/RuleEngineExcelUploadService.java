package com.project.ProjectS.service;

import com.project.ProjectS.model.*;
import com.project.ProjectS.exception.DuplicateRuleEngineException;
import com.project.ProjectS.exception.RuleEngineUploadException;
import com.project.ProjectS.processor.RuleEngineExcelProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RuleEngineExcelUploadService {
    private static final Logger log = LogManager.getLogger(RuleEngineExcelUploadService.class);
    private final RuleEngineExcelProcessor processor;

    public RuleEngineExcelUploadService(RuleEngineExcelProcessor processor) { this.processor = processor; }

    // Each proxied processor invocation commits or rolls back independently.
    public RuleEngineUploadResponseDTO process(List<Map<String, String>> rows) {
        RuleEngineUploadResponseDTO response = new RuleEngineUploadResponseDTO();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            if (row.entrySet().stream().filter(e -> !"_excel_row_number".equals(e.getKey()))
                    .allMatch(e -> e.getValue() == null || e.getValue().isBlank())) continue;
            int rowNumber = i + 2;
            try { rowNumber = Integer.parseInt(row.getOrDefault("_excel_row_number", String.valueOf(rowNumber))); }
            catch (NumberFormatException ignored) { /* Use physical list position if metadata is invalid. */ }
            response.setTotalRows(response.getTotalRows() + 1);
            try {
                RuleEngineUploadErrorDTO error = processor.processRow(row, rowNumber);
                if (error == null) {
                    response.setRulesUploaded(response.getRulesUploaded() + 1);
                } else {
                    response.getErrors().add(error);
                    if (error.attributeCreated()) response.setCreatedAttributes(response.getCreatedAttributes() + 1);
                }
            } catch (Exception e) {
                log.error("Rule Engine upload failed at Excel row {}", rowNumber, e);
                String field = "row", name = row.get("attribute"), header = row.get("field_type");
                if (e instanceof RuleEngineUploadException validation) {
                    field = validation.getField(); name = validation.getAttributeName(); header = validation.getHeaderName();
                } else if (e instanceof DuplicateRuleEngineException) {
                    field = "attribute";
                }
                response.getErrors().add(new RuleEngineUploadErrorDTO(rowNumber, field, name, header,
                        "Excel row " + rowNumber + ": " + e.getMessage(), false));
            }
        }
        response.setSkippedRows(response.getErrors().size());
        response.setSuccess(response.getSkippedRows() == 0);
        response.setMessage(response.getRulesUploaded() + " rules uploaded, " + response.getSkippedRows()
                + " rows skipped, " + response.getCreatedAttributes() + " attributes created as DRAFT."
                + (response.getSkippedRows() > 0 ? " Review the errors and retry only skipped rows." : ""));
        return response;
    }
}
