package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RuleEngineResponseDTO {

    private Long ruleEngineId;

    private Long chapterId;

    private String chapterName;

    private Long pairAttributeId;

    private String pairAttributeName;



    private String pairAttributeTableHeaderName;

    private String fieldName;

    private String fieldType;

    private String relationshipName;

    private Integer pairOrder;

    private String arithmetic1;

    private Long table1Id;

    private String table1Name;

    private Long header1Id;

    private String header1Name;

    private String amountPosition1;

    private String information1;

    private String arithmetic2;

    private Long table2Id;

    private String table2Name;

    private Long header2Id;

    private String header2Name;

    private String amountPosition2;

    private String information2;

    private String arithmetic3;

    private Long table3Id;

    private String table3Name;

    private Long header3Id;

    private String header3Name;

    private String amountPosition3;

    private String information3;

    private String arithmetic4;

    private Long table4Id;

    private String table4Name;

    private Long header4Id;

    private String header4Name;

    private String amountPosition4;

    private String information4;

    private Boolean activeRow;

    private Integer rowStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
