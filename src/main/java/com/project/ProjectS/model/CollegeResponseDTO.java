package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CollegeResponseDTO {

    private Long collegeId;
    private String instituteName;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean activeRow;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}