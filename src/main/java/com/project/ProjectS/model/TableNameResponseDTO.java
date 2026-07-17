package com.project.ProjectS.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableNameResponseDTO {

    private Long tableNameId;
    private String name;
    private Boolean activeRow;
    private Integer rowStatus;

}