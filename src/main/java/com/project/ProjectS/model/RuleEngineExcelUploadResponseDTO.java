package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RuleEngineExcelUploadResponseDTO {

    private int totalRows;

    private int rulesUploaded;

    private int draftsCreated;

    private int failedRows;

    private List<RuleEngineDraftDTO> drafts = new ArrayList<>();

    private List<RuleEngineFailedRowDTO> failed = new ArrayList<>();
}