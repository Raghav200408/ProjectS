package com.project.ProjectS.controller;

import com.project.ProjectS.model.ChapterRequestDTO;
import com.project.ProjectS.model.ChapterResponseDTO;
import com.project.ProjectS.service.ChapterService;
import com.project.ProjectS.service.ExcelUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.project.ProjectS.processor.ChapterExcelProcessor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chapter")
public class ChapterController {
    @Autowired
    public ChapterController(ChapterService service, ChapterExcelProcessor chapterExcelProcessor, ExcelUploadService excelUploadService) {
        this.service = service;
        this.chapterExcelProcessor = chapterExcelProcessor;
        this.excelUploadService = excelUploadService;
    }


    private static final Logger logger =
            LogManager.getLogger(ChapterController.class);
    private final ChapterService service;
    private final ChapterExcelProcessor chapterExcelProcessor;
    private final ExcelUploadService excelUploadService;

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
    @PostMapping("/upload")
    public ResponseEntity<String> uploadChapter(
            @RequestParam("file") MultipartFile file) {


        logger.info("Received request to upload chapter Excel file.");


        try {


            List<Map<String,String>> excelData =
                    excelUploadService.readExcel(file);



            chapterExcelProcessor.process(excelData);



            logger.info(
                    "Chapter Excel uploaded successfully."
            );


            return new ResponseEntity<>(
                    "Chapter Excel uploaded successfully",
                    HttpStatus.OK
            );


        } catch (Exception e) {


            logger.error(
                    "Chapter Excel upload failed",
                    e
            );


            return new ResponseEntity<>(
                    "Chapter Excel upload failed : "
                            + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

    }
}
