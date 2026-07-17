package com.project.ProjectS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollegeRequestDTO {

    private String instituteName;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean activeRow;

}