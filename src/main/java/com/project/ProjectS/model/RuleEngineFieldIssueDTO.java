package com.project.ProjectS.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RuleEngineFieldIssueDTO {

    private String field;

    private String value;

    private String message;
}