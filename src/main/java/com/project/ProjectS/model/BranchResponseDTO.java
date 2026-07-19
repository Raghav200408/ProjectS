package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BranchResponseDTO {

    private Long branchId;

    private Long collegeId;
    private String collegeName;

    private String branchName;
    private String address;
    private String phoneNumber;
    private String email;

    private Boolean activeRow;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}