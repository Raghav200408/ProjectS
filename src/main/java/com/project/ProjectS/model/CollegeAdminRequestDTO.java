package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollegeAdminRequestDTO {

    private String  name;
    private Long employeeId;
    private String designation;
    private Long collegeId;
    private String email;
    private String  phoneNumber;
    private String    password;
    private String   address;
}
