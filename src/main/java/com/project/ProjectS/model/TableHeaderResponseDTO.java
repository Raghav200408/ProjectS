package com.project.ProjectS.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableHeaderResponseDTO {

    private Long header_id;
    private String name;
    private Boolean activeRow;
    private Integer rowStatus;
}
