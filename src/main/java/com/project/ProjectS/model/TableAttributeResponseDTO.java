package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class TableAttributeResponseDTO {
    private Long attributeId;
    private String name;

    private String rowStatus;
    private Boolean activeRow;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String tableHeaderName;
    private Long amount1;
    private Long amount2;
}
