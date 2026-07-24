package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminRequestDTO {
    private String name;
    private Long  employeeId;
    private String designation;
    private String email;
    private String phoneNumber;
    private String password;
    private String   address;
}
