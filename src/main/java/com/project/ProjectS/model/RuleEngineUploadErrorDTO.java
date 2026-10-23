package com.project.ProjectS.model;

public record RuleEngineUploadErrorDTO(int rowNumber, String field, String attributeName,
                                       String headerName, String errorMessage, boolean attributeCreated) {}
