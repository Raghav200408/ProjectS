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
    @GetMapping("/superAdmins")
    public ResponseEntity<List<UserResponseDTO>> getAllSuperAdmins() {

        List<UserResponseDTO> superAdmins =
                userService.getAllSuperAdmins();

        return ResponseEntity.ok(superAdmins);
    }
    @GetMapping("/branchAdmins")
    public ResponseEntity<List<UserResponseDTO>> getAllBranchAdmins(){
        List<UserResponseDTO> branchAdmins = userService.getAllBranchAdmins();
        return ResponseEntity.ok(branchAdmins);
    }
    @PutMapping("/superAdmin/{userId}")
    public ResponseEntity<UserResponseDTO> updateSuperAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody SuperAdminRequestDTO request) {

        UserResponseDTO response =
                userService.updateSuperAdmin(userId, request);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/branchAdmin/{userId}")
    public ResponseEntity<UserResponseDTO> updateBranchAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody BranchAdminRequestDTO request) {

        UserResponseDTO response =
                userService.updateBranchAdmin(userId, request);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/student/{userId}")
    public ResponseEntity<UserResponseDTO> updateStudent(
            @PathVariable Long userId,
            @Valid @RequestBody StudentRequestDTO request) {

        UserResponseDTO response =
                userService.updateStudent(userId, request);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/guest/{userId}")
    public ResponseEntity<UserResponseDTO> updateGuest(
            @PathVariable Long userId,
            @Valid @RequestBody GuestUserRequestDTO request) {

        UserResponseDTO response =
                userService.updateGuest(userId, request);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/superAdmin/{userId}")
    public ResponseEntity<UserResponseDTO> getSuperAdminById(
            @PathVariable Long userId) {

        UserResponseDTO response =
                userService.getSuperAdminById(userId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/branchAdmin/{userId}")
    public ResponseEntity<UserResponseDTO> getBranchAdminById(
            @PathVariable Long userId) {

        UserResponseDTO response =
                userService.getBranchAdminById(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<UserResponseDTO> getStudentById(
            @PathVariable Long userId) {

        UserResponseDTO response =
                userService.getStudentById(userId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/guest/{userId}")
    public ResponseEntity<UserResponseDTO> getGuestById(
            @PathVariable Long userId) {

        UserResponseDTO response =
                userService.getGuestById(userId);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/superAdmin/{userId}")
    public ResponseEntity<String> deleteSuperAdmin(
            @PathVariable Long userId) {

        String response = userService.deleteSuperAdmin(userId);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/branchAdmin/{userId}")
    public ResponseEntity<String> deleteBranchAdmin(
            @PathVariable Long userId) {

        String response = userService.deleteBranchAdmin(userId);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/student/{userId}")
    public ResponseEntity<String> deleteStudent(
            @PathVariable Long userId) {

        String response = userService.deleteStudent(userId);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/guest/{userId}")
    public ResponseEntity<String> deleteGuest(
            @PathVariable Long userId) {

        String response = userService.deleteGuest(userId);

        return ResponseEntity.ok(response);
    }

}