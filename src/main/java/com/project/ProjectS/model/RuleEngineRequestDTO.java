package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleEngineRequestDTO {

    private String chapterName;

    private String pairAttributeName;

    private String fieldName;

    private String fieldType;

    private String relationshipName;

    private Integer pairOrder;



    private String arithmetic1;

    private String table1Name;

    private String header1Name;

    private String amountPosition1;

    private String information1;

    private String arithmetic2;

    private String table2Name;

    private String header2Name;

    private String amountPosition2;

    private String information2;



    private String arithmetic3;

    private String table3Name;

    private String header3Name;

    private String amountPosition3;

    private String information3;



    private String arithmetic4;

    private String table4Name;

    private String header4Name;

    private String amountPosition4;

    private String information4;

    private Boolean activeRow;

    private Integer rowStatus;
}
