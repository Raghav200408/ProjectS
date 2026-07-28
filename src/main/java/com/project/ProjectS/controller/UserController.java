package com.project.ProjectS.controller;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.*;
import com.project.ProjectS.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;




    @PostMapping("/superAdmin")
    public ResponseEntity<UserResponseDTO> createSuperAdmin(
            @Valid @RequestBody SuperAdminRequestDTO request) {

        UserResponseDTO response =
                userService.createSuperAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }




    @PostMapping("/branchAdmin")
    public ResponseEntity<UserResponseDTO> createBranchAdmin(
            @Valid @RequestBody BranchAdminRequestDTO request) {

        UserResponseDTO response =
                userService.createBranchAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/student")
    public ResponseEntity<UserResponseDTO> createStudent(
            @Valid @RequestBody StudentRequestDTO request) {

        UserResponseDTO response =
                userService.createStudent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/guest/register")
    public ResponseEntity<UserResponseDTO> registerGuest(
            @Valid @RequestBody GuestUserRequestDTO request) {

        UserResponseDTO response =
                userService.registerGuest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping("/students")
    public  ResponseEntity<List<UserResponseDTO>> getAllStudents() {
        List<UserResponseDTO> students = userService. getAllStudents();


        return ResponseEntity.ok(students);


    }
    @GetMapping("/branchAdmins")
    public ResponseEntity<List<UserResponseDTO>> getAllBranchAdmins(){
        List<UserResponseDTO> branchAdmins = userService.getAllBranchAdmins();
        return ResponseEntity.ok(branchAdmins);
    }

}