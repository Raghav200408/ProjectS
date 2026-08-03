package com.project.ProjectS.controller;

import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.model.TableNameRequestDTO;
import com.project.ProjectS.model.TableNameResponseDTO;
import com.project.ProjectS.processor.TableNameExcelProcessor;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.TableNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table-names")
public class TableNameController {

    @Autowired
    private TableNameService service;

    @Autowired
    private ExcelUploadService excelUploadService;

    @Autowired
    private TableNameExcelProcessor tableNameExcelProcessor;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody TableNameRequestDTO request) {

        String response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TableName>> getAll() {

        List<TableName> response = service.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableName> getById(@PathVariable Long id) {

        TableName response = service.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody TableNameRequestDTO request) {

        String response = service.update(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        String response = service.delete(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //upload
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        try {

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);

            tableNameExcelProcessor.process(excelData);

            return ResponseEntity.ok("Excel uploaded successfully");

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}