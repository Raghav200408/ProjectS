package com.project.ProjectS.exception;

public class RuleEngineUploadException extends IllegalArgumentException {
    private final String field;
    private final String attributeName;
    private final String headerName;

    public RuleEngineUploadException(String field, String attributeName, String headerName, String message) {
        super(message);
        this.field = field;
        this.attributeName = attributeName;
        this.headerName = headerName;
    }

    public String getField() { return field; }
    public String getAttributeName() { return attributeName; }
    public String getHeaderName() { return headerName; }
}
