package com.project.ProjectS.controller;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.mapper.BranchExcelMapper;
import com.project.ProjectS.model.BranchRequestDTO;
import com.project.ProjectS.model.BranchResponseDTO;
import com.project.ProjectS.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.GenericExcelUploadService;

import java.util.Map;


import java.util.List;
@RestController
@RequestMapping("/api/branch")
public class BranchController {
    @Autowired
    public BranchController(BranchExcelMapper branchMapper, BranchService service, ExcelUploadService excelUploadService, GenericExcelUploadService genericExcelUploadService, BranchRepository branchRepository) {
        this.branchMapper = branchMapper;
        this.service = service;
        this.excelUploadService = excelUploadService;
        this.genericExcelUploadService = genericExcelUploadService;
        this.branchRepository = branchRepository;
    }


    private static final Logger logger =
            LogManager.getLogger(BranchController.class);
    private final BranchExcelMapper branchMapper;
    private final BranchService service;
    private final ExcelUploadService excelUploadService;
    private final GenericExcelUploadService genericExcelUploadService;
    private final BranchRepository branchRepository;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody BranchRequestDTO request) {

        logger.info("Received request to create branch.");

        String response = service.create(request);

        logger.info("Create branch request completed successfully.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<BranchResponseDTO> getAll() {

        logger.info("Received request to fetch all branches.");

        List<BranchResponseDTO> branches = service.getAll();

        logger.info("Fetched {} branches successfully.", branches.size());

        return branches;
    }

    @GetMapping("/{id}")
    public Branch getById(@PathVariable Long id) {

        logger.info("Received request to fetch branch with ID: {}", id);

        Branch branch = service.getById(id);

        logger.info("Branch fetched successfully with ID: {}", id);

        return branch;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequestDTO request) {

        logger.info("Received request to update branch with ID: {}", id);

        String response = service.update(id, request);

        logger.info("Branch updated successfully with ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        logger.info("Received request to delete branch with ID: {}", id);

        String response = service.delete(id);

        logger.info("Branch deleted successfully with ID: {}", id);

        return response;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadBranch(
            @RequestParam("file") MultipartFile file) {

        try {

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);

            genericExcelUploadService.process(
                    excelData,
                    branchMapper,
                    branchRepository
            );

            return ResponseEntity.ok("Branch Excel uploaded successfully");

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


}
