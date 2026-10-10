package com.project.ProjectS.controller;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.model.TableHeaderRequestDTO;
import com.project.ProjectS.processor.TableHeaderExcelProcessor;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.TableHeaderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table-headers")
public class TableHeaderController {
    private static final Logger log = LogManager.getLogger(TableHeaderController.class);

    @Autowired
    public TableHeaderController(TableHeaderService service, ExcelUploadService excelUploadService, TableHeaderExcelProcessor tableHeaderExcelProcessor) {
        this.service = service;
        this.excelUploadService = excelUploadService;
        this.tableHeaderExcelProcessor = tableHeaderExcelProcessor;
    }

    private final TableHeaderService service;
    private final ExcelUploadService excelUploadService;
    private final TableHeaderExcelProcessor tableHeaderExcelProcessor;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody TableHeaderRequestDTO request) {
        log.info("Creating table header: name={}", request.getName());
        String response = service.create(request);
        log.info("Table header created successfully: name={}", request.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TableHeader>> getAll() {
        log.info("Fetching all table headers");
        List<TableHeader> response = service.getAll();
        log.info("Fetched {} table headers", response.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableHeader> getById(@PathVariable Long id) {
        log.info("Fetching table header by id={}", id);
        TableHeader response = service.getById(id);
        log.info("Table header fetched successfully: id={}", id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody TableHeaderRequestDTO request) {
        log.info("Updating table header: id={} name={}", id, request.getName());
        String response = service.update(id, request);
        log.info("Table header updated successfully: id={}", id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        log.info("Deleting table header: id={}", id);
        String response = service.delete(id);
        log.info("Table header deleted successfully: id={}", id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading table-header Excel file: filename={} size={}", file.getOriginalFilename(), file.getSize());
        try {
            List<Map<String, String>> excelData = excelUploadService.readExcel(file);
            tableHeaderExcelProcessor.process(excelData);
            log.info("Table-header Excel upload completed successfully");
            return ResponseEntity.ok("Excel uploaded successfully");
        } catch (Exception e) {
            log.error("Table-header Excel upload failed: filename={}", file.getOriginalFilename(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
