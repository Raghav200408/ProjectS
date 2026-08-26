package com.project.ProjectS.controller;

import com.project.ProjectS.entity.RuleEngine;
import com.project.ProjectS.model.RuleEngineRequestDTO;
import com.project.ProjectS.model.RuleEngineResponse;
import com.project.ProjectS.model.RuleEngineResponseDTO;
import com.project.ProjectS.service.RuleEngineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.project.ProjectS.processor.RuleEngineExcelProcessor;
import com.project.ProjectS.service.ExcelUploadService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rule-engines")
public class RuleEngineController {
    @Autowired
    public RuleEngineController(RuleEngineService service, ExcelUploadService excelUploadService, RuleEngineExcelProcessor ruleEngineExcelProcessor) {
        this.service = service;
        this.excelUploadService = excelUploadService;
        this.ruleEngineExcelProcessor = ruleEngineExcelProcessor;
    }

    private final RuleEngineService service;
    private final ExcelUploadService excelUploadService;
    private final RuleEngineExcelProcessor ruleEngineExcelProcessor;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody RuleEngineRequestDTO request) {

        String response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RuleEngineResponseDTO>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleEngineResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody RuleEngineRequestDTO request) {

        String response = service.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping("/excel/upload")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        try {

            // Step 1: Read Excel file
            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);


            // Step 2: Process Excel data and save RuleEngine
            ruleEngineExcelProcessor.process(excelData);


            return ResponseEntity.ok(
                    "Rule Engine Excel uploaded successfully"
            );


        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Excel upload failed : "
                                    + e.getMessage()
                    );
        }
    }

    @GetMapping("/attribute/{attributeId}")
    public List<RuleEngineResponse> getRuleEngine(
            @PathVariable Long attributeId) {
        return service.getRuleEngineByAttributeId(attributeId);
    }
}

