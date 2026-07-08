package com.project.ProjectS.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.service.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO dto) {

        userService.save(dto);
        return new ResponseEntity<>("User registered successfully.", HttpStatus.CREATED);
    }

    
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {

        UserDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id,
                                             @RequestBody UserDTO dto) {

        userService.update(id, dto);
        return ResponseEntity.ok("User updated successfully.");
    }

 
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {

        userService.delete(id);
        return ResponseEntity.ok("User deleted successfully.");
    }
    @PostMapping("/google-register")
    public ResponseEntity<String> googleRegister(
            @RequestBody UserDTO dto) {

        if(userService.existsByEmail(dto.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("User already registered.");

        }

        userService.registerGoogleUser(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Google user registered successfully.");

    }
}