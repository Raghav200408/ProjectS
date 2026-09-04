package com.project.ProjectS.controller;

import com.project.ProjectS.model.TopicRequestDTO;
import com.project.ProjectS.model.TopicResponseDTO;
import com.project.ProjectS.processor.TopicExcelProcessor;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    @Autowired
    public TopicController(
            TopicService service,
            TopicExcelProcessor topicExcelProcessor,
            ExcelUploadService excelUploadService) {
        this.service = service;
        this.topicExcelProcessor = topicExcelProcessor;
        this.excelUploadService = excelUploadService;
    }

    private final TopicService service;
    private final TopicExcelProcessor topicExcelProcessor;
    private final ExcelUploadService excelUploadService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody TopicRequestDTO request) {

        String response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TopicResponseDTO>> getAll() {

        List<TopicResponseDTO> response = service.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicResponseDTO> getById(@PathVariable Long id) {

        TopicResponseDTO response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody TopicRequestDTO request) {

        String response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        String response = service.delete(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadTopic(
            @RequestParam("file") MultipartFile file) {

        try {

            List<Map<String,String>> excelData =
                    excelUploadService.readExcel(file);

            topicExcelProcessor.process(excelData);

            return ResponseEntity.ok(
                    "Topic Excel uploaded successfully"
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "Topic Excel upload failed : "
                                    + e.getMessage()
                    );
        }
    }
}
