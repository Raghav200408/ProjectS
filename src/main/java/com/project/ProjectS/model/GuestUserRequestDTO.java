package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestUserRequestDTO {
    private String name;
    private String password;
    private String email;
    private String address;
    private String phoneNumber;
}
