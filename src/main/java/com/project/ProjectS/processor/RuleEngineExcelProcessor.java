package com.project.ProjectS.processor;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineExcelMapper;
import com.project.ProjectS.model.RuleEngineDraftDTO;
import com.project.ProjectS.model.RuleEngineExcelUploadResponseDTO;
import com.project.ProjectS.model.RuleEngineFailedRowDTO;
import com.project.ProjectS.model.RuleEngineFieldIssueDTO;
import com.project.ProjectS.repository.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Transactional
public class RuleEngineExcelProcessor {

    private static final Logger logger =
            LogManager.getLogger(RuleEngineExcelProcessor.class);

    private final RuleEngineExcelMapper ruleEngineMapper;
    private final RuleEngineRepository ruleEngineRepository;
    private final ChapterRepository chapterRepository;
    private final TableAttributeRepository tableAttributeRepository;
    private final TableHeaderRepository tableHeaderRepository;
    private final TableNameRepository tableNameRepository;

    @Autowired
    public RuleEngineExcelProcessor(
            RuleEngineExcelMapper ruleEngineMapper,
            RuleEngineRepository ruleEngineRepository,
            ChapterRepository chapterRepository,
            TableAttributeRepository tableAttributeRepository,
            TableHeaderRepository tableHeaderRepository,
            TableNameRepository tableNameRepository) {

        this.ruleEngineMapper = ruleEngineMapper;
        this.ruleEngineRepository = ruleEngineRepository;
        this.chapterRepository = chapterRepository;
        this.tableAttributeRepository = tableAttributeRepository;
        this.tableHeaderRepository = tableHeaderRepository;
        this.tableNameRepository = tableNameRepository;
    }

    public RuleEngineExcelUploadResponseDTO process(
            List<Map<String, String>> excelData) {

        RuleEngineExcelUploadResponseDTO response =
                new RuleEngineExcelUploadResponseDTO();
        if (excelData == null || excelData.isEmpty()) {
            return response;
        }

        response.setTotalRows(excelData.size());
        int excelRowNumber = 2;
        for (Map<String, String> row : excelData) {

            int currentRowNumber = excelRowNumber++;

            if (row == null ||
                    row.values().stream()
                            .allMatch(value ->
                                    value == null || value.isBlank())) {

                continue;
            }

            try {

                RuleEngine ruleEngine =
                        ruleEngineMapper.map(row);

                List<RuleEngineFieldIssueDTO> issues =
                        new ArrayList<>();
                String chapterName =
                        clean(row.get("chapter"));

                if (chapterName == null) {

                    issues.add(
                            new RuleEngineFieldIssueDTO(
                                    "chapter",
                                    null,
                                    "Chapter is required"
                            )
                    );

                } else {

                    Chapter chapter =
                            chapterRepository
                                    .findByName(chapterName)
                                    .orElse(null);

                    if (chapter == null) {

                        issues.add(
                                new RuleEngineFieldIssueDTO(
                                        "chapter",
                                        chapterName,
                                        "Chapter not found"
                                )
                        );

                    } else {

                        ruleEngine.setChapter(chapter);
                    }
                }
                String pairAttributeName =
                        clean(row.get("pair_attribute"));

                if (pairAttributeName == null) {

                    issues.add(
                            new RuleEngineFieldIssueDTO(
                                    "pair_attribute",
                                    null,
                                    "Pair Attribute is required"
                            )
                    );

                } else {

                    TableAttribute pairAttribute =
                            tableAttributeRepository
                                    .findByName(pairAttributeName)
                                    .orElse(null);

                    if (pairAttribute == null) {

                        issues.add(
                                new RuleEngineFieldIssueDTO(
                                        "pair_attribute",
                                        pairAttributeName,
                                        "Pair Attribute not found"
                                )
                        );

                    } else {

                        ruleEngine.setPairAttribute(pairAttribute);
                    }
                }

                String attributeName =
                        clean(row.get("attribute"));

                if (attributeName == null) {

                    issues.add(
                            new RuleEngineFieldIssueDTO(
                                    "attribute",
                                    null,
                                    "Attribute is required"
                            )
                    );

                } else {

                    TableAttribute attribute =
                            tableAttributeRepository
                                    .findByName(attributeName)
                                    .orElse(null);

                    if (attribute == null) {

                        issues.add(
                                new RuleEngineFieldIssueDTO(
                                        "attribute",
                                        attributeName,
                                        "Attribute not found"
                                )
                        );

                    } else {

                        ruleEngine.setTableAttributeid(attribute);
                    }
                }
                String relationshipName =
                        clean(row.get("relationship_name"));

                if (relationshipName == null) {

                    issues.add(
                            new RuleEngineFieldIssueDTO(
                                    "relationship_name",
                                    null,
                                    "Relationship is required"
                            )
                    );

                } else {

                    ruleEngine.setRelationshipName(
                            relationshipName
                    );
                }

                String pairOrder =
                        clean(row.get("pair_order"));

                if (pairOrder == null) {

                    issues.add(
                            new RuleEngineFieldIssueDTO(
                                    "pair_order",
                                    null,
                                    "Pair Order is required"
                            )
                    );

                } else {

                    try {

                        ruleEngine.setPairOrder(
                                Integer.parseInt(pairOrder)
                        );

                    } catch (NumberFormatException e) {

                        issues.add(
                                new RuleEngineFieldIssueDTO(
                                        "pair_order",
                                        pairOrder,
                                        "Pair Order must be a number"
                                )
                        );
                    }
                }
                validateRequiredCondition(
                        row,
                        ruleEngine,
                        1,
                        issues
                );

                validateOptionalCondition(
                        row,
                        ruleEngine,
                        2,
                        issues
                );

                validateOptionalCondition(
                        row,
                        ruleEngine,
                        3,
                        issues
                );

                validateOptionalCondition(
                        row,
                        ruleEngine,
                        4,
                        issues
                );
                validateRequiredTableAndHeader(
                        row,
                        ruleEngine,
                        1,
                        issues
                );
                validateOptionalTableAndHeader(
                        row,
                        ruleEngine,
                        2,
                        issues
                );
                validateOptionalTableAndHeader(
                        row,
                        ruleEngine,
                        3,
                        issues
                );

                validateOptionalTableAndHeader(
                        row,
                        ruleEngine,
                        4,
                        issues
                );

                if (issues.isEmpty()) {

                    ruleEngine.setRowStatus(1);

                } else {
                    ruleEngine.setRowStatus(0);
                }

                if (ruleEngine.getChapter() == null) {

                    response.setFailedRows(
                            response.getFailedRows() + 1
                    );

                    response.getFailed().add(
                            new RuleEngineFailedRowDTO(
                                    currentRowNumber,
                                    "Chapter is required and must exist"
                            )
                    );

                    logger.warn(
                            "Skipping Excel row {} because chapter is invalid",
                            currentRowNumber
                    );

                    continue;
                }
                RuleEngine savedRule =
                        ruleEngineRepository.save(ruleEngine);

                logger.info(
                        "Rule Engine saved. ID={}, row={}, status={}",
                        savedRule.getRuleEngineId(),
                        currentRowNumber,
                        savedRule.getRowStatus()
                );

                if (issues.isEmpty()) {

                    response.setRulesUploaded(
                            response.getRulesUploaded() + 1
                    );

                }

                else {

                    response.setDraftsCreated(
                            response.getDraftsCreated() + 1
                    );

                    RuleEngineDraftDTO draft =
                            new RuleEngineDraftDTO();

                    draft.setRowNumber(
                            currentRowNumber
                    );

                    draft.setRuleEngineId(
                            savedRule.getRuleEngineId()
                    );

                    draft.setStatus("DRAFT");

                    draft.setMissingFields(
                            issues
                    );

                    response.getDrafts().add(
                            draft
                    );
                }

            } catch (Exception e) {
                logger.error(
                        "Error processing Excel row {}",
                        currentRowNumber,
                        e
                );

                response.setFailedRows(
                        response.getFailedRows() + 1
                );

                response.getFailed().add(
                        new RuleEngineFailedRowDTO(
                                currentRowNumber,
                                e.getMessage()
                        )
                );
            }
        }

        return response;
    }

    private void validateRequiredCondition(
            Map<String, String> row,
            RuleEngine ruleEngine,
            int index,
            List<RuleEngineFieldIssueDTO> issues) {

        String arithmetic =
                clean(row.get("arithmetic" + index));

        String amountPosition =
                clean(row.get("amount_position" + index));

        String information =
                clean(row.get("information" + index));

        if (arithmetic == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "arithmetic" + index,
                            null,
                            "Arithmetic is required"
                    )
            );

        } else {

            setArithmetic(
                    ruleEngine,
                    index,
                    arithmetic
            );
        }

        if (amountPosition == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "amount_position" + index,
                            null,
                            "Amount Position is required"
                    )
            );

        } else {

            setAmountPosition(
                    ruleEngine,
                    index,
                    amountPosition
            );
        }
        if (information == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "information" + index,
                            null,
                            "Information is required"
                    )
            );

        } else {

            setInformation(
                    ruleEngine,
                    index,
                    information
            );
        }
    }
    private void validateOptionalCondition(
            Map<String, String> row,
            RuleEngine ruleEngine,
            int index,
            List<RuleEngineFieldIssueDTO> issues) {

        String arithmetic =
                clean(row.get("arithmetic" + index));

        String amountPosition =
                clean(row.get("amount_position" + index));

        String information =
                clean(row.get("information" + index));
        boolean conditionProvided =
                arithmetic != null ||
                        amountPosition != null ||
                        information != null;

        if (!conditionProvided) {
            return;
        }

        if (arithmetic == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "arithmetic" + index,
                            null,
                            "Arithmetic is required"
                    )
            );

        } else {

            setArithmetic(
                    ruleEngine,
                    index,
                    arithmetic
            );
        }
        if (amountPosition == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "amount_position" + index,
                            null,
                            "Amount Position is required"
                    )
            );

        } else {

            setAmountPosition(
                    ruleEngine,
                    index,
                    amountPosition
            );
        }

        if (information == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "information" + index,
                            null,
                            "Information is required"
                    )
            );

        } else {

            setInformation(
                    ruleEngine,
                    index,
                    information
            );
        }
    }
    private void setArithmetic(
            RuleEngine ruleEngine,
            int index,
            String value) {

        switch (index) {

            case 1 ->
                    ruleEngine.setArithmetic1(value);

            case 2 ->
                    ruleEngine.setArithmetic2(value);

            case 3 ->
                    ruleEngine.setArithmetic3(value);

            case 4 ->
                    ruleEngine.setArithmetic4(value);

            default ->
                    throw new IllegalArgumentException(
                            "Invalid condition index: " + index
                    );
        }
    }

    private void setAmountPosition(
            RuleEngine ruleEngine,
            int index,
            String value) {

        switch (index) {

            case 1 ->
                    ruleEngine.setAmountPosition1(value);

            case 2 ->
                    ruleEngine.setAmountPosition2(value);

            case 3 ->
                    ruleEngine.setAmountPosition3(value);

            case 4 ->
                    ruleEngine.setAmountPosition4(value);

            default ->
                    throw new IllegalArgumentException(
                            "Invalid condition index: " + index
                    );
        }
    }

    private void setInformation(
            RuleEngine ruleEngine,
            int index,
            String value) {

        switch (index) {

            case 1 ->
                    ruleEngine.setInformation1(value);

            case 2 ->
                    ruleEngine.setInformation2(value);

            case 3 ->
                    ruleEngine.setInformation3(value);

            case 4 ->
                    ruleEngine.setInformation4(value);

            default ->
                    throw new IllegalArgumentException(
                            "Invalid condition index: " + index
                    );
        }
    }
    private void validateRequiredTableAndHeader(
            Map<String, String> row,
            RuleEngine ruleEngine,
            int index,
            List<RuleEngineFieldIssueDTO> issues) {

        String tableName =
                clean(row.get("table" + index));

        String headerName =
                clean(row.get("header" + index));

        if (tableName == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "table" + index,
                            null,
                            "Table is required"
                    )
            );

        } else {

            TableName table =
                    tableNameRepository
                            .findByName(tableName)
                            .orElse(null);

            if (table == null) {

                issues.add(
                        new RuleEngineFieldIssueDTO(
                                "table" + index,
                                tableName,
                                "Table not found"
                        )
                );

            } else {

                setTable(
                        ruleEngine,
                        index,
                        table
                );
            }
        }

        if (headerName == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "header" + index,
                            null,
                            "Header is required"
                    )
            );

        } else {

            TableHeader header =
                    tableHeaderRepository
                            .findByName(headerName)
                            .orElse(null);

            if (header == null) {

                issues.add(
                        new RuleEngineFieldIssueDTO(
                                "header" + index,
                                headerName,
                                "Header not found"
                        )
                );

            } else {

                setHeader(
                        ruleEngine,
                        index,
                        header
                );
            }
        }
    }

    private void validateOptionalTableAndHeader(
            Map<String, String> row,
            RuleEngine ruleEngine,
            int index,
            List<RuleEngineFieldIssueDTO> issues) {

        String tableName =
                clean(row.get("table" + index));

        String headerName =
                clean(row.get("header" + index));

        boolean tableOrHeaderProvided =
                tableName != null ||
                        headerName != null;

        if (!tableOrHeaderProvided) {
            return;
        }

        if (tableName == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "table" + index,
                            null,
                            "Table is required"
                    )
            );

        } else {

            TableName table =
                    tableNameRepository
                            .findByName(tableName)
                            .orElse(null);

            if (table == null) {

                issues.add(
                        new RuleEngineFieldIssueDTO(
                                "table" + index,
                                tableName,
                                "Table not found"
                        )
                );

            } else {

                setTable(
                        ruleEngine,
                        index,
                        table
                );
            }
        }

        if (headerName == null) {

            issues.add(
                    new RuleEngineFieldIssueDTO(
                            "header" + index,
                            null,
                            "Header is required"
                    )
            );

        } else {

            TableHeader header =
                    tableHeaderRepository
                            .findByName(headerName)
                            .orElse(null);

            if (header == null) {

                issues.add(
                        new RuleEngineFieldIssueDTO(
                                "header" + index,
                                headerName,
                                "Header not found"
                        )
                );

            } else {

                setHeader(
                        ruleEngine,
                        index,
                        header
                );
            }
        }
    }

    private void setTable(
            RuleEngine ruleEngine,
            int index,
            TableName table) {

        switch (index) {

            case 1 ->
                    ruleEngine.setTable1(table);

            case 2 ->
                    ruleEngine.setTable2(table);

            case 3 ->
                    ruleEngine.setTable3(table);

            case 4 ->
                    ruleEngine.setTable4(table);

            default ->
                    throw new IllegalArgumentException(
                            "Invalid table index: " + index
                    );
        }
    }
    private void setHeader(
            RuleEngine ruleEngine,
            int index,
            TableHeader header) {

        switch (index) {

            case 1 ->
                    ruleEngine.setHeader1(header);

            case 2 ->
                    ruleEngine.setHeader2(header);

            case 3 ->
                    ruleEngine.setHeader3(header);

            case 4 ->
                    ruleEngine.setHeader4(header);

            default ->
                    throw new IllegalArgumentException(
                            "Invalid header index: " + index
                    );
        }
    }
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