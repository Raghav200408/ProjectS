package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.RuleEngine;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RuleEngineExcelMapper
        implements ExcelRowMapper<RuleEngine> {

    @Override
    public RuleEngine map(Map<String, String> row) {

        RuleEngine ruleEngine = new RuleEngine();

        ruleEngine.setRelationshipName(
                row.get("relationship_name")
        );

        ruleEngine.setArithmetic1(
                row.get("arithmetic1")
        );

        ruleEngine.setAmountPosition1(
                row.get("amount_position1")
        );

        ruleEngine.setInformation1(
                row.get("information1")
        );

        ruleEngine.setArithmetic2(
                row.get("arithmetic2")
        );

        ruleEngine.setAmountPosition2(
                row.get("amount_position2")
        );

        ruleEngine.setInformation2(
                row.get("information2")
        );

        ruleEngine.setArithmetic3(
                row.get("arithmetic3")
        );

        ruleEngine.setAmountPosition3(
                row.get("amount_position3")
        );

        ruleEngine.setInformation3(
                row.get("information3")
        );

        ruleEngine.setArithmetic4(
                row.get("arithmetic4")
        );

        ruleEngine.setAmountPosition4(
                row.get("amount_position4")
        );

        ruleEngine.setInformation4(
                row.get("information4")
        );
        String activeRow = row.get("active_row");

        if (activeRow != null && !activeRow.isBlank()) {
            ruleEngine.setActiveRow(
                    Boolean.parseBoolean(activeRow)
            );
        }
        return ruleEngine;
    }
}