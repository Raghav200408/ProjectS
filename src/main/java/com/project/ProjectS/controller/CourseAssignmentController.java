package com.project.ProjectS.controller;

import com.project.ProjectS.model.CourseAssignmentDTO;
import com.project.ProjectS.service.CourseAssignmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/course/{courseId}/assignments")
public class CourseAssignmentController {
    private final CourseAssignmentService service;
    public CourseAssignmentController(CourseAssignmentService service) { this.service = service; }
    public record AssignmentRequest(@NotNull Long collegeId, @NotNull Long branchId, @NotNull Boolean activeRow) {}
    @GetMapping public List<CourseAssignmentDTO> list(@PathVariable Long courseId) { return service.list(courseId, service.currentUser()); }
    @PutMapping public CourseAssignmentDTO assign(@PathVariable Long courseId, @Valid @RequestBody AssignmentRequest request) {
        return service.assign(courseId, request.collegeId(), request.branchId(), request.activeRow());
    }
}
