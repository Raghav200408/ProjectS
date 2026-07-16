package com.project.ProjectS.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.LoginRequestDTO;
import com.project.ProjectS.model.LoginResponseDTO;
import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.project.ProjectS.security.jwt.JwtUtil;
import com.project.ProjectS.security.service.CustomUserDetails;
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/users")
public class UserController {
	private static final Logger logger =
	        LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO dto) {

        if(userService.existsByEmail(dto.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Email already registered");
        }

        userService.save(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");

        }

        String email = authentication.getName();

        UserDTO user = userService.findByEmail(email);

        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");

        }

        return ResponseEntity.ok(user);

    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {

        UserDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserDTO dto) {

        userService.update(id, dto);
        return ResponseEntity.ok("User updated successfully.");
    }

 
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {

        userService.delete(id);
        return ResponseEntity.ok("User deleted successfully.");
    }
    @PostMapping("/google-register")
    public ResponseEntity<?> googleRegister(
            @RequestBody UserDTO dto) {

        if (userService.existsByEmail(dto.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("User already registered.");

        }

        // Save Google user
        userService.registerGoogleUser(dto);

        // Fetch the saved user
        UserDTO user =
                userService.findByEmail(dto.getEmail());

        String role = switch (user.getRoleId()) {

            case 1 -> "ROLE_SUPER_ADMIN";
            case 2 -> "ROLE_BRANCH_ADMIN";
            default -> "ROLE_STUDENT";

        };

        // Generate JWT
        String token =
                jwtUtil.generateToken(
                        user.getEmail(),
                        role
                );

        // Build response
        LoginResponseDTO response =
                new LoginResponseDTO();

        response.setRegistered(true);
        response.setUser(user);
        response.setToken(token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(

                        new UsernamePasswordAuthenticationToken(

                                request.getEmail(),
                                request.getPassword()

                        )

                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        UserDTO user =
                userDetails.getUser();

        String token =
                jwtUtil.generateToken(

                        user.getEmail(),

                        userDetails
                                .getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority()

                );

        LoginResponseDTO response = new LoginResponseDTO();

        response.setToken(token);

        response.setUser(user);

        response.setRegistered(true);

        return ResponseEntity.ok(response);

    }
   
}