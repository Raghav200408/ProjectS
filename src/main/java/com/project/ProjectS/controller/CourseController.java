package com.project.ProjectS.controller;

import com.project.ProjectS.model.CourseRequestDTO;
import com.project.ProjectS.model.CourseResponseDTO;
import com.project.ProjectS.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
@RestController
@RequestMapping("/api/course")
public class CourseController {

    private static final Logger logger =
            LogManager.getLogger(CourseController.class);

    @Autowired
    private CourseService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody CourseRequestDTO request) {

        logger.info("Received request to create course.");

        String response = service.create(request);

        logger.info("Create course request completed successfully.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CourseResponseDTO> getAll() {

        logger.info("Received request to fetch all courses.");

        List<CourseResponseDTO> courses = service.getAll();

        logger.info("Fetched {} courses successfully.", courses.size());

        return courses;
    }

    @GetMapping("/{id}")
    public CourseResponseDTO getById(@PathVariable Long id) {

        logger.info("Received request to fetch course with ID: {}", id);

        CourseResponseDTO course = service.getById(id);

        logger.info("Course fetched successfully with ID: {}", id);

        return course;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDTO request) {

        logger.info("Received request to update course with ID: {}", id);

        String response = service.update(id, request);

        logger.info("Course updated successfully with ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        logger.info("Received request to delete course with ID: {}", id);

        String response = service.delete(id);

        logger.info("Course deleted successfully with ID: {}", id);

        return response;
    }
}