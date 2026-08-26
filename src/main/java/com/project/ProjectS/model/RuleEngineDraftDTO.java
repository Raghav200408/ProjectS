package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RuleEngineDraftDTO {

    private int rowNumber;

    private Long ruleEngineId;

    private String status;

    private List<RuleEngineFieldIssueDTO> missingFields = new ArrayList<>();
}