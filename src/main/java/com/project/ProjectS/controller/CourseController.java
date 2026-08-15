package com.project.ProjectS.controller;

import com.project.ProjectS.mapper.CourseExcelMapper;
import com.project.ProjectS.model.CourseRequestDTO;
import com.project.ProjectS.model.CourseResponseDTO;
import com.project.ProjectS.processor.CourseExcelProcessor;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.service.CourseService;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.GenericExcelUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
public class CourseController {
    @Autowired
    public CourseController(CourseService service, CourseExcelProcessor courseExcelProcessor, GenericExcelUploadService genericExcelUploadService, ExcelUploadService excelUploadService, CourseRepository courseRepository, CourseExcelMapper courseExcelMapper) {
        this.service = service;
        this.courseExcelProcessor = courseExcelProcessor;
        this.genericExcelUploadService = genericExcelUploadService;
        this.excelUploadService = excelUploadService;
        this.courseRepository = courseRepository;
        this.courseExcelMapper = courseExcelMapper;
    }


    private static final Logger logger =
            LogManager.getLogger(CourseController.class);
    private final CourseService service;
    private final CourseExcelProcessor courseExcelProcessor;
    private final GenericExcelUploadService genericExcelUploadService;
    private final ExcelUploadService excelUploadService;
    private final CourseRepository courseRepository;
    private final CourseExcelMapper courseExcelMapper;

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

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCourse(
            @RequestParam("file") MultipartFile file){


        try{


            List<Map<String,String>> excelData =
                    excelUploadService.readExcel(file);



            courseExcelProcessor.process(excelData);



            return ResponseEntity.ok(
                    "Course Excel uploaded successfully"
            );


        }
        catch(Exception e){

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());

        }

    }
}
