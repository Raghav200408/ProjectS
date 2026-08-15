package com.project.ProjectS.controller;

import com.project.ProjectS.model.QuestionCategoryRequestDTO;
import com.project.ProjectS.model.QuestionCategoryResponseDTO;
import com.project.ProjectS.processor.QuestionCategoryExcelProcessor;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.QuestionCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question-categories")
public class QuestionCategoryController {
    @Autowired
    public QuestionCategoryController(QuestionCategoryService service, QuestionCategoryExcelProcessor questionCategoryExcelProcessor, ExcelUploadService excelUploadService) {
        this.service = service;
        this.questionCategoryExcelProcessor = questionCategoryExcelProcessor;
        this.excelUploadService = excelUploadService;
    }

    private final QuestionCategoryService service;
    private final QuestionCategoryExcelProcessor questionCategoryExcelProcessor;
    private final ExcelUploadService excelUploadService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody QuestionCategoryRequestDTO request) {

        String response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<QuestionCategoryResponseDTO>> getAll() {

        List<QuestionCategoryResponseDTO> response = service.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionCategoryResponseDTO> getById(@PathVariable Long id) {

        QuestionCategoryResponseDTO response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody QuestionCategoryRequestDTO request) {

        String response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        String response = service.delete(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadQuestionCategory(
            @RequestParam("file") MultipartFile file) {

        try {

            List<Map<String,String>> excelData =
                    excelUploadService.readExcel(file);

            questionCategoryExcelProcessor.process(excelData);

            return ResponseEntity.ok(
                    "Question Category Excel uploaded successfully"
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "Question Category Excel upload failed : "
                                    + e.getMessage()
                    );
        }
    }
}
