package com.project.ProjectS.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RuleEngineFailedRowDTO {

    private int rowNumber;

    private String message;
}