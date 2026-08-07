package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleEngineResponse {

    private Long ruleEngineId;

    private Long chapterId;
    private String chapterName;

    private Long attributeId;
    private String attributeName;

    private Long pairAttributeId;
    private String pairAttributeName;

    private String relationshipName;

    private Integer pairOrder;

    private RuleConditionDTO condition1;

    private RuleConditionDTO condition2;

    private RuleConditionDTO condition3;

    private RuleConditionDTO condition4;

    private Boolean activeRow;

    private Integer rowStatus;


}
