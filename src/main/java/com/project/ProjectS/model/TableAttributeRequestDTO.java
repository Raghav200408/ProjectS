package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableAttributeRequestDTO {

    private String name;

    private String tableHeaderName;
    private Long amount1;
    private Long amount2;

    // NEW FIELD
    private Boolean rowDisable;
}