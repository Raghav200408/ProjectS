package com.project.ProjectS.controller;

import com.project.ProjectS.model.ChapterRequestDTO;
import com.project.ProjectS.model.ChapterResponseDTO;
import com.project.ProjectS.service.ChapterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@RestController
@RequestMapping("/api/chapter")
public class ChapterController {

    private static final Logger logger =
            LogManager.getLogger(ChapterController.class);

    @Autowired
    private ChapterService service;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody ChapterRequestDTO request) {

        logger.info("Received request to create chapter.");

        String response = service.create(request);

        logger.info("Create chapter request completed successfully.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<ChapterResponseDTO> getAll() {

        logger.info("Received request to fetch all chapters.");

        List<ChapterResponseDTO> chapters = service.getAll();

        logger.info("Fetched {} chapters successfully.", chapters.size());

        return chapters;
    }

    @GetMapping("/{id}")
    public ChapterResponseDTO getById(@PathVariable Long id) {

        logger.info("Received request to fetch chapter with ID: {}", id);

        ChapterResponseDTO chapter = service.getById(id);

        logger.info("Chapter fetched successfully with ID: {}", id);

        return chapter;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody ChapterRequestDTO request) {

        logger.info("Received request to update chapter with ID: {}", id);

        String response = service.update(id, request);

        logger.info("Chapter updated successfully with ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        logger.info("Received request to delete chapter with ID: {}", id);

        String response = service.delete(id);

        logger.info("Chapter deleted successfully with ID: {}", id);

        return response;
    }
}