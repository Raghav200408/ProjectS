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

        // Direct fields

        ruleEngine.setRelationshipName(
                row.get("relationship_name")
        );

        String pairOrder = row.get("pair_order");

        if (pairOrder != null && !pairOrder.trim().isEmpty()) {

            ruleEngine.setPairOrder(
                    Integer.parseInt(pairOrder.trim())
            );
        }

        // Condition 1

        ruleEngine.setArithmetic1(
                row.get("arithmetic1")
        );

        ruleEngine.setAmountPosition1(
                row.get("amount_position1")
        );

        ruleEngine.setInformation1(
                row.get("information1")
        );

        // Condition 2

        ruleEngine.setArithmetic2(
                row.get("arithmetic2")
        );

        ruleEngine.setAmountPosition2(
                row.get("amount_position2")
        );

        ruleEngine.setInformation2(
                row.get("information2")
        );

        // Condition 3

        ruleEngine.setArithmetic3(
                row.get("arithmetic3")
        );

        ruleEngine.setAmountPosition3(
                row.get("amount_position3")
        );

        ruleEngine.setInformation3(
                row.get("information3")
        );

        // Condition 4

        ruleEngine.setArithmetic4(
                row.get("arithmetic4")
        );

        ruleEngine.setAmountPosition4(
                row.get("amount_position4")
        );

        ruleEngine.setInformation4(
                row.get("information4")
        );
        /*
         * Relationship fields are not mapped here:
         *
         * chapter
         * pairAttribute
         * fieldName
         * fieldType
         * table1
         * header1
         * table2
         * header2
         * table3
         * header3
         * table4
         * header4
         *
         * These will be handled in RuleEngineExcelProcessor
         */
        return ruleEngine;
    }
}