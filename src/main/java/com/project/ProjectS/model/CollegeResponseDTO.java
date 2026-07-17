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