package com.project.ProjectS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableNameRequestDTO {

    @NotBlank(message = "Table name is required")
    private String name;

}