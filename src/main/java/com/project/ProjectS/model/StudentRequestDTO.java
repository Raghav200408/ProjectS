package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequestDTO {
   private String   name;
    private Long  studentCode;
    private Long    collegeId;
    private Long   branchId;
    private Long  sectionId;
    private String  guardianName;
    private String guardianPhoneNumber;
    private String  email;
    private String phoneNumber;
    private String   password;
    private String   address;
}
