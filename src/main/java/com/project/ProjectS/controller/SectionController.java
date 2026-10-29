package com.project.ProjectS.controller;

import com.project.ProjectS.mapper.SectionExcelMapper;
import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/section")
public class SectionController {
    @Autowired
    public SectionController(SectionService service, ExcelUploadService excelUploadService, SectionExcelMapper sectionExcelMapper) {
        this.service = service;
        this.excelUploadService = excelUploadService;
        this.sectionExcelMapper = sectionExcelMapper;
    }


    private static final Logger log =
            LogManager.getLogger(SectionController.class);
    private final SectionService service;
    private final ExcelUploadService excelUploadService;
    private final SectionExcelMapper sectionExcelMapper;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody SectionRequestDTO request,
            Authentication authentication) {

        log.info("Received request to create section.");

        String response = service.create(request, authentication);

        log.info("Create section request completed successfully.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<SectionResponseDTO> getAll(Authentication authentication) {

        log.info("Received request to fetch all sections.");

        List<SectionResponseDTO> sections = service.getAll(authentication);

        log.info("Fetched {} sections successfully.", sections.size());

        return sections;
    }

    @GetMapping("/{id}")
    public SectionResponseDTO getById(@PathVariable Long id, Authentication authentication) {

        log.info("Received request to fetch section with ID: {}", id);

        SectionResponseDTO section = service.getById(id, authentication);

        log.info("Section fetched successfully with ID: {}", id);

        return section;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody SectionRequestDTO request,
            Authentication authentication) {

        log.info("Received request to update section with ID: {}", id);

        String response = service.update(id, request, authentication);

        log.info("Section updated successfully with ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, Authentication authentication) {

        log.info("Received request to delete section with ID: {}", id);

        String response = service.delete(id, authentication);

        log.info("Section deleted successfully with ID: {}", id);

        return response;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadSection(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {


        log.info("Received request to upload section Excel file.");


        try {


            List<Map<String,String>> excelData =
                    excelUploadService.readExcel(file);



            for (Map<String, String> row : excelData) {
                if (row.values().stream().allMatch(String::isBlank)) continue;
                service.importSection(sectionExcelMapper.map(row), authentication);
            }



            log.info(
                    "Section Excel upload completed successfully."
            );


            return ResponseEntity.ok(
                    "Section Excel uploaded successfully"
            );


        }
        catch(org.springframework.security.access.AccessDeniedException e){
            throw e;
        }
        catch(Exception e){


            log.error(
                    "Section Excel upload failed",
                    e
            );


            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());

        }

    }
}
