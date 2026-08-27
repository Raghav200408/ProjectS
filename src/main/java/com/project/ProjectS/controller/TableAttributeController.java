package com.project.ProjectS.controller;

import com.project.ProjectS.model.TableAttributeRequestDTO;
import com.project.ProjectS.model.TableAttributeResponseDTO;
import com.project.ProjectS.processor.TableAttributeExcelProcessor;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.TableAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table-attributes")
public class TableAttributeController {
    @Autowired
    public TableAttributeController(TableAttributeService service, ExcelUploadService excelUploadService, TableAttributeExcelProcessor tableAttributeExcelProcessor) {
        this.service = service;
        this.excelUploadService = excelUploadService;
        this.tableAttributeExcelProcessor = tableAttributeExcelProcessor;
    }

    private final TableAttributeService service;
    private final ExcelUploadService excelUploadService;
    private final TableAttributeExcelProcessor tableAttributeExcelProcessor;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody TableAttributeRequestDTO request) {

        String response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TableAttributeResponseDTO>> getAll() {

        List<TableAttributeResponseDTO> response = service.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableAttributeResponseDTO> getById(@PathVariable Long id) {

        TableAttributeResponseDTO response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody TableAttributeRequestDTO request) {

        String response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        String response = service.delete(id);
        return ResponseEntity.ok(response);
    }

    // Excel Upload
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        try {

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);

            tableAttributeExcelProcessor.process(excelData);

            return ResponseEntity.ok("Excel uploaded successfully");

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rule")
    public ResponseEntity<List<TableAttributeResponseDTO>>
    getRuleAttributes() {

        List<TableAttributeResponseDTO> response =
                service.getRuleAttributes();

        return ResponseEntity.ok(response);
    }
}
