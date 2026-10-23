package com.project.ProjectS.processor;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineExcelMapper;
import com.project.ProjectS.model.RuleEngineUploadErrorDTO;
import com.project.ProjectS.exception.RuleEngineUploadException;
import com.project.ProjectS.repository.*;
import com.project.ProjectS.util.RuleEngineUniqueness;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;

@Component
public class RuleEngineExcelProcessor {
    private final RuleEngineExcelMapper ruleEngineMapper;
    private final RuleEngineRepository ruleEngineRepository;
    private final ChapterRepository chapterRepository;
    private final TableAttributeRepository tableAttributeRepository;
    private final TableHeaderRepository tableHeaderRepository;
    private final TableNameRepository tableNameRepository;

    public RuleEngineExcelProcessor(RuleEngineExcelMapper ruleEngineMapper, RuleEngineRepository ruleEngineRepository,
            ChapterRepository chapterRepository, TableAttributeRepository tableAttributeRepository,
            TableHeaderRepository tableHeaderRepository, TableNameRepository tableNameRepository) {
        this.ruleEngineMapper = ruleEngineMapper; this.ruleEngineRepository = ruleEngineRepository;
        this.chapterRepository = chapterRepository; this.tableAttributeRepository = tableAttributeRepository;
        this.tableHeaderRepository = tableHeaderRepository; this.tableNameRepository = tableNameRepository;
    }

    // Returning an error commits a newly created DRAFT; throwing rolls back only this row.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RuleEngineUploadErrorDTO processRow(Map<String, String> row, int rowNumber) {
        String chapterName = clean(row.get("chapter"));
        if (chapterName == null) throw new IllegalArgumentException("chapter is required");
        Chapter chapter = chapterRepository.findByName(chapterName)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found: " + chapterName));

        String attributeName = requiredName(row, "attribute");
        TableHeader fieldType = header(row, "field_type", attributeName);
        Optional<TableAttribute> attribute = findAttribute(attributeName, fieldType, "attribute");
        if (attribute.isEmpty()) return createDraft(rowNumber, "attribute", attributeName, fieldType);

        // Skip existing rules before inspecting or creating any paired attribute.
        RuleEngineUniqueness.requireAvailable(ruleEngineRepository, chapter, attribute.get(), null);

        // Do not inspect or create the paired attribute until the main attribute already exists.
        String pairName = requiredName(row, "pair_attribute");
        TableHeader pairedFieldType = header(row, "paired_field_type", pairName);
        Optional<TableAttribute> pair = findAttribute(pairName, pairedFieldType, "pair_attribute");
        if (pair.isEmpty()) return createDraft(rowNumber, "pair_attribute", pairName, pairedFieldType);

        RuleEngine rule = ruleEngineMapper.map(row);
        rule.setChapter(chapter);
        rule.setFieldType(fieldType);
        rule.setPairedFieldType(pairedFieldType);
        rule.setTableAttributeid(attribute.get());
        rule.setPairAttribute(pair.get());
        for (int index = 1; index <= 4; index++) setTableAndHeader(row, rule, index);
        RuleEngineUniqueness.save(ruleEngineRepository, rule);
        attribute.get().setRowStatus("RULE");
        tableAttributeRepository.save(attribute.get());
        return null;
    }

    private String requiredName(Map<String, String> row, String field) {
        String name = clean(row.get(field));
        if (name == null) throw new RuleEngineUploadException(field, null, null, field + " is required");
        return name;
    }

    private TableHeader header(Map<String, String> row, String field, String attributeName) {
        String name = clean(row.get(field));
        if (name == null) throw new RuleEngineUploadException(field, attributeName, null, field + " is required");
        try {
            return tableHeaderRepository.findByName(name).orElseThrow(() -> new RuleEngineUploadException(
                    field, attributeName, name, field + " '" + name + "' not found. No attribute created."));
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new RuleEngineUploadException(field, attributeName, name,
                    field + " '" + name + "' matched multiple headers. No attribute created.");
        }
    }

    private Optional<TableAttribute> findAttribute(String name, TableHeader header, String field) {
        try {
            return tableAttributeRepository.findByNameAndTableHeader(name, header);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new RuleEngineUploadException(field, name, header.getName(), field + " '" + name
                    + "' with header '" + header.getName() + "' matched multiple records. No attribute created.");
        }
    }

    private RuleEngineUploadErrorDTO createDraft(int rowNumber, String field, String name, TableHeader header) {
        TableAttribute attribute = new TableAttribute();
        attribute.setName(name); attribute.setTableHeader(header); attribute.setRowStatus("DRAFT");
        tableAttributeRepository.saveAndFlush(attribute);
        return new RuleEngineUploadErrorDTO(rowNumber, field, name, header.getName(),
                "Excel row " + rowNumber + ": " + field + " '" + name + "' not found with header '"
                        + header.getName() + "'. Created in Table Attributes as DRAFT. Rule row skipped; retry this row.", true);
    }

    // Table + Header Mapping

    private void setTableAndHeader(
            Map<String, String> row,
            RuleEngine ruleEngine,
            int index
    ) {

        // Table
        // Excel: table1, table2, table3, table4

        String tableName =
                clean(
                        row.get(
                                "table" + index
                        )
                );

        if (tableName != null) {

            TableName table =
                    tableNameRepository
                            .findByName(tableName)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Table not found : "
                                                    + tableName
                                    )
                            );

            switch (index) {

                case 1 -> ruleEngine.setTable1(table);

                case 2 -> ruleEngine.setTable2(table);

                case 3 -> ruleEngine.setTable3(table);

                case 4 -> ruleEngine.setTable4(table);
            }
        }

        // Header
        // Excel: header1, header2, header3, header4

        String headerName =
                clean(
                        row.get(
                                "header" + index
                        )
                );

        if (headerName != null) {

            TableHeader header =
                    tableHeaderRepository
                            .findByName(headerName)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Header not found : "
                                                    + headerName
                                    )
                            );

            switch (index) {

                case 1 -> ruleEngine.setHeader1(header);

                case 2 -> ruleEngine.setHeader2(header);

                case 3 -> ruleEngine.setHeader3(header);

                case 4 -> ruleEngine.setHeader4(header);
            }
        }
    }

    // Clean Excel Value

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        value = value.trim();

        return value.isEmpty()
                ? null
                : value;
    }
}
