package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDTO {

    private Long userId;

    private String name;

    private String designation;

    private String address;

    private Long employeeId;

    private Long  studentCode;


    private Long collegeId;
    private String collegeName;

    private Long branchId;
    private String branchName;


    private Long sectionId;
    private String sectionName;

    private String email;

    private String phoneNumber;

    private String guardianName;

    private String guardianPhoneNumber;


    private Integer roleId;
    private String roleName;


    private String googleId;

    private String profilePicture;

    private String loginType;



    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}