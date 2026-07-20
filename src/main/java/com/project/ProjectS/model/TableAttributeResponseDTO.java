package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class TableAttributeResponseDTO {
    private Long attributeId;
    private String name;
    private String shortName;
    private Boolean rowStatus;
    private Boolean activeRow;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String tableHeaderName;
}
