package com.project.ProjectS.processor;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineExcelMapper;
import com.project.ProjectS.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@Transactional
public class RuleEngineExcelProcessor implements ExcelProcessor {

    @Autowired
    public RuleEngineExcelProcessor(RuleEngineExcelMapper ruleEngineMapper, RuleEngineRepository ruleEngineRepository, ChapterRepository chapterRepository, TableAttributeRepository tableAttributeRepository, TableHeaderRepository tableHeaderRepository, TableNameRepository tableNameRepository) {
        this.ruleEngineMapper = ruleEngineMapper;
        this.ruleEngineRepository = ruleEngineRepository;
        this.chapterRepository = chapterRepository;
        this.tableAttributeRepository = tableAttributeRepository;
        this.tableHeaderRepository = tableHeaderRepository;
        this.tableNameRepository = tableNameRepository;
    }


    private static final Logger logger = LogManager.getLogger(RuleEngineExcelProcessor.class);
    private final RuleEngineExcelMapper ruleEngineMapper;
    private final RuleEngineRepository ruleEngineRepository;
    private final ChapterRepository chapterRepository;
    private final TableAttributeRepository tableAttributeRepository;
    private final TableHeaderRepository tableHeaderRepository;
    private final TableNameRepository tableNameRepository;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            // Skip empty rows
            if (row.values().stream().allMatch(
                    value -> value == null || value.isBlank()
            )) {
                continue;
            }

            RuleEngine ruleEngine =
                    ruleEngineMapper.map(row);

            // Chapter
            String chapterName =
                    clean(row.get("chapter"));

            if (chapterName == null) {
                throw new RuntimeException(
                        "chapter is required"
                );
            }

            Chapter chapter =
                    chapterRepository.findByName(chapterName)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Chapter not found : "
                                                    + chapterName
                                    )
                            );

            ruleEngine.setChapter(chapter);

            // Pair Attribute
            String pairAttributeName =
                    clean(row.get("pair_attribute"));

            if (pairAttributeName != null) {

                TableAttribute pairAttribute =
                        tableAttributeRepository
                                .findByName(pairAttributeName)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Pair Attribute not found : "
                                                        + pairAttributeName
                                        )
                                );

                ruleEngine.setPairAttribute(
                        pairAttribute
                );
            }

            // Attribute

            String attributeName =
                    clean(row.get("attribute"));

            if (attributeName != null) {

                TableAttribute attribute =
                        tableAttributeRepository
                                .findByName(attributeName)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Attribute not found : "
                                                        + attributeName
                                        )
                                );

                ruleEngine.setTableAttributeid(
                        attribute
                );
            }

            // Pair Order

            String pairOrder =
                    clean(row.get("pair_order"));

            if (pairOrder != null) {

                ruleEngine.setPairOrder(
                        Integer.parseInt(pairOrder)
                );
            }

            // Conditions 1 - 4

            setTableAndHeader(
                    row,
                    ruleEngine,
                    1
            );

            setTableAndHeader(
                    row,
                    ruleEngine,
                    2
            );

            setTableAndHeader(
                    row,
                    ruleEngine,
                    3
            );

            setTableAndHeader(
                    row,
                    ruleEngine,
                    4
            );

            // Save Rule Engine

            System.out.println(
                    "Saving Rule Engine : "
                            + ruleEngine.getRelationshipName()
            );

            RuleEngine savedRule =
                    ruleEngineRepository.save(ruleEngine);

            System.out.println(
                    "========== SAVED RULE ENGINE ID : "
                            + savedRule.getRuleEngineId()
                            + " =========="
            );
        }
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