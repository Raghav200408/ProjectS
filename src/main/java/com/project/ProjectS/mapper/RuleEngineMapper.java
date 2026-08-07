package com.project.ProjectS.mapper;

import org.springframework.stereotype.Component;

import com.project.ProjectS.entity.RuleEngine;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.model.RuleConditionDTO;
import com.project.ProjectS.model.RuleEngineResponse;

@Component
public class RuleEngineMapper {

    public RuleEngineResponse toResponse(RuleEngine rule) {

        RuleEngineResponse response = new RuleEngineResponse();

        response.setRuleEngineId(rule.getRuleEngineId());

        // Chapter
        if (rule.getChapter() != null) {
            response.setChapterId(rule.getChapter().getChapterId());
            response.setChapterName(rule.getChapter().getName());
        }

        // Attribute
        if (rule.getTableAttributeid() != null) {
            response.setAttributeId(rule.getTableAttributeid().getAttributeId());
            response.setAttributeName(rule.getTableAttributeid().getName());
        }

        // Pair Attribute
        if (rule.getPairAttribute() != null) {
            response.setPairAttributeId(rule.getPairAttribute().getAttributeId());
            response.setPairAttributeName(rule.getPairAttribute().getName());
        }

        response.setRelationshipName(rule.getRelationshipName());
        response.setPairOrder(rule.getPairOrder());

        response.setCondition1(buildCondition(
                rule.getArithmetic1(),
                rule.getTable1(),
                rule.getHeader1(),
                rule.getAmountPosition1(),
                rule.getInformation1()));

        response.setCondition2(buildCondition(
                rule.getArithmetic2(),
                rule.getTable2(),
                rule.getHeader2(),
                rule.getAmountPosition2(),
                rule.getInformation2()));

        response.setCondition3(buildCondition(
                rule.getArithmetic3(),
                rule.getTable3(),
                rule.getHeader3(),
                rule.getAmountPosition3(),
                rule.getInformation3()));

        response.setCondition4(buildCondition(
                rule.getArithmetic4(),
                rule.getTable4(),
                rule.getHeader4(),
                rule.getAmountPosition4(),
                rule.getInformation4()));

        response.setActiveRow(rule.getActiveRow());
        response.setRowStatus(rule.getRowStatus());

        return response;
    }

    private RuleConditionDTO buildCondition(
            String arithmetic,
            TableName table,
            TableHeader header,
            String amountPosition,
            String information) {

        RuleConditionDTO dto = new RuleConditionDTO();

        dto.setArithmetic(arithmetic);

        if (table != null) {
            dto.setTableId(table.getTableNameId());
            dto.setTableName(table.getName());
        }

        if (header != null) {
            dto.setHeaderId(header.getHeaderId());
            dto.setHeaderName(header.getName());
        }

        dto.setAmountPosition(amountPosition);
        dto.setInformation(information);

        return dto;
    }
}