package com.project.ProjectS.controller;

import com.project.ProjectS.entity.College;
import com.project.ProjectS.model.CollegeRequestDTO;
import com.project.ProjectS.model.CollegeResponseDTO;
import com.project.ProjectS.service.CollegeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@RestController
@RequestMapping("/api/college")
public class CollegeController {

    private static final Logger logger =
            LogManager.getLogger(CollegeController.class);

    @Autowired
    private CollegeService service;

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CollegeRequestDTO request) {

        logger.info("Received request to create college.");

        String response = service.create(request);


        logger.info("Create college request completed successfully.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CollegeResponseDTO> getAll() {

        logger.info("Received request to fetch all colleges.");

        List<CollegeResponseDTO> colleges = service.getAll();

        logger.info("Fetched {} colleges successfully.", colleges.size());
        return colleges;
    }

    @GetMapping("/{id}")
    public College getById(@PathVariable Long id) {

        logger.info("Received request to fetch college with ID: {}", id);

        College college = service.getById(id);

        logger.info("College fetched successfully with ID: {}", id);
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @Valid @RequestBody CollegeRequestDTO request) {

        logger.info("Received request to update college with ID: {}", id);
        String response = service.update(id, request);

        logger.info("College updated successfully with ID: {}", id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {


        logger.info("Received request to delete college with ID: {}", id);

        String response = service.delete(id);

        logger.info("College deleted successfully with ID: {}", id);
        return service.delete(id);
    }
}