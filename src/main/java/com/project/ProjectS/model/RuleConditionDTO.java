package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleConditionDTO {

    private String arithmetic;

    private Long tableId;
    private String tableName;

    private Long headerId;
    private String headerName;

    private String amountPosition;

    private String information;
}
