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
public class BranchRequestDTO {

    private Long collegeId;
    private String branchName;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean activeRow;

}