package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RuleEngineUploadResponseDTO {
    private int totalRows;
    private int rulesUploaded;
    private int skippedRows;
    private int createdAttributes;
    private boolean success;
    private String message;
    private List<RuleEngineUploadErrorDTO> errors = new ArrayList<>();
}
