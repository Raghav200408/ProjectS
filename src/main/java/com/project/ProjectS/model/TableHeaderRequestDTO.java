package com.project.ProjectS.model;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableHeaderRequestDTO {
    @NotBlank(message = "Table header is required")
    private String name;
}
