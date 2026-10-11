package com.project.ProjectS.controller;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.WeeklyStudyGoalDTO;
import com.project.ProjectS.model.WeeklyStudyGoalRequest;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/study-goals/weekly")
public class StudentStudyGoalController {
    private final UserRepository userRepository;

    public StudentStudyGoalController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<WeeklyStudyGoalDTO> getGoal(
            @AuthenticationPrincipal CustomUserDetails principal) {
        User student = requireStudent(principal.getUser());
        return ResponseEntity.ok(new WeeklyStudyGoalDTO(student.getWeeklyStudyGoalHours()));
    }

    @PutMapping
    public ResponseEntity<WeeklyStudyGoalDTO> saveGoal(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody WeeklyStudyGoalRequest request) {
        User student = requireStudent(principal.getUser());
        student.setWeeklyStudyGoalHours(request.weeklyGoalHours());
        User saved = userRepository.save(student);
        return ResponseEntity.ok(new WeeklyStudyGoalDTO(saved.getWeeklyStudyGoalHours()));
    }

    private User requireStudent(User user) {
        if (user.getRole() == null || !"STUDENT".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return user;
    }
}
