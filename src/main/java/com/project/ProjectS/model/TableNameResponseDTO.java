package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableNameResponseDTO {

    private Long headerId;
    private String name;
    private Boolean activeRow;
    private Integer rowStatus;

}