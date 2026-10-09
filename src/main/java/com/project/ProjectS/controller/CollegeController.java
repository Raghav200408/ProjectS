package com.project.ProjectS.controller;

import com.project.ProjectS.entity.College;
import com.project.ProjectS.model.CollegeRequestDTO;
import com.project.ProjectS.model.CollegeResponseDTO;
import com.project.ProjectS.service.CollegeService;
import com.project.ProjectS.service.ExcelUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.project.ProjectS.mapper.CollegeExcelMapper;

import com.project.ProjectS.repository.CollegeRepository;
import com.project.ProjectS.service.GenericExcelUploadService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/college")
public class CollegeController {
    @Autowired
    public CollegeController(CollegeExcelMapper collegeMapper, CollegeService service, GenericExcelUploadService genericExcelUploadService, CollegeRepository collegeRepository, ExcelUploadService excelUploadService) {
        this.collegeMapper = collegeMapper;
        this.service = service;
        this.genericExcelUploadService = genericExcelUploadService;
        this.collegeRepository = collegeRepository;
        this.excelUploadService = excelUploadService;
    }


    private static final Logger log =
            LogManager.getLogger(CollegeController.class);
    private final CollegeExcelMapper collegeMapper;
    private final CollegeService service;
    private final GenericExcelUploadService genericExcelUploadService;
    private final CollegeRepository collegeRepository;
    private final ExcelUploadService excelUploadService;

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CollegeRequestDTO request) {

        log.info("Received request to create college.");

        String response = service.create(request);


        log.info("Create college request completed successfully.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CollegeResponseDTO> getAll() {

        log.info("Received request to fetch all colleges.");

        List<CollegeResponseDTO> colleges = service.getAll();

        log.info("Fetched {} colleges successfully.", colleges.size());
        return colleges;
    }

    @GetMapping("/{id}")
    public College getById(@PathVariable Long id) {

        log.info("Received request to fetch college with ID: {}", id);

        College college = service.getById(id);

        log.info("College fetched successfully with ID: {}", id);
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @Valid @RequestBody CollegeRequestDTO request) {

        log.info("Received request to update college with ID: {}", id);
        String response = service.update(id, request);

        log.info("College updated successfully with ID: {}", id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {


        log.info("Received request to delete college with ID: {}", id);

        String response = service.delete(id);

        log.info("College deleted successfully with ID: {}", id);

        return response;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCollege(
            @RequestParam("file") MultipartFile file) {

        try {

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);

            genericExcelUploadService.process(
                    excelData,
                    collegeMapper,
                    collegeRepository
            );

            return ResponseEntity.ok("College Excel uploaded successfully");

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
