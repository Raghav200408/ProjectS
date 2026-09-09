package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingPairRequestDTO {

    private String columnA;

    private String columnB;

    private Integer displayOrder;
}