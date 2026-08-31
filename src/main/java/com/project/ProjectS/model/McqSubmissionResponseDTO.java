package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class McqSubmissionResponseDTO {

    private BigDecimal score;

    private List<McqSubmissionResultDTO> results;
}