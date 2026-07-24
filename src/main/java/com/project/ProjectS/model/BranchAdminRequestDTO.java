package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchAdminRequestDTO {

    private String  name;
    private Long employeeId;
    private String designation;
    private Long collegeId;
    private Long branchId;
    private String email;
    private String  phoneNumber;
    private String    password;
    private String   address;
}
