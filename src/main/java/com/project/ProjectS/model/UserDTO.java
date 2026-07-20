package com.project.ProjectS.model;


import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Integer userId;
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;


    @NotNull(message = "Role is required")
    private Integer roleId;
  
    private String designation;
    private String address;
    private String employeeId;

    private String studentCode;
    private String className;
    private String college;
    private String branch;
    private String section;

   
    private String phoneNumber;
  

    private String guardianName;
    private String guardianPhoneNumber;

   
    private String roleName;

    
}
