package com.project.ProjectS.model;

import java.time.LocalDateTime;

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