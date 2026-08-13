package com.project.ProjectS.controller;

import com.project.ProjectS.mapper.AttendanceExcelMapper;
import com.project.ProjectS.model.AttendanceRequestDTO;
import com.project.ProjectS.model.AttendanceResponseDTO;
import com.project.ProjectS.repository.AttendanceRepository;
import com.project.ProjectS.service.AttendanceService;
import com.project.ProjectS.service.ExcelUploadService;
import com.project.ProjectS.service.GenericExcelUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceExcelMapper attendanceMapper;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private GenericExcelUploadService genericExcelUploadService;

    @Autowired
    private ExcelUploadService excelUploadService;

    // Create Attendance
    @PostMapping
    public ResponseEntity<AttendanceResponseDTO> createAttendance(
            @RequestBody AttendanceRequestDTO requestDTO) {

        AttendanceResponseDTO response =
                attendanceService.createAttendance(requestDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get Attendance by ID
    @GetMapping("/{attendanceId}")
    public ResponseEntity<AttendanceResponseDTO> getAttendanceById(
            @PathVariable Long attendanceId) {

        AttendanceResponseDTO response =
                attendanceService.getAttendanceById(attendanceId);

        return ResponseEntity.ok(response);
    }

    // Get All Attendance
    @GetMapping
    public ResponseEntity<List<AttendanceResponseDTO>> getAllAttendance() {

        List<AttendanceResponseDTO> response =
                attendanceService.getAllAttendance();

        return ResponseEntity.ok(response);
    }

    // Update Attendance
    @PutMapping("/{attendanceId}")
    public ResponseEntity<AttendanceResponseDTO> updateAttendance(
            @PathVariable Long attendanceId,
            @RequestBody AttendanceRequestDTO requestDTO) {

        AttendanceResponseDTO response =
                attendanceService.updateAttendance(
                        attendanceId,
                        requestDTO
                );

        return ResponseEntity.ok(response);
    }

    // Delete Attendance
    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<String> deleteAttendance(
            @PathVariable Long attendanceId) {

        attendanceService.deleteAttendance(attendanceId);

        return ResponseEntity.ok(
                "Attendance deleted successfully"
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAttendance(
            @RequestParam("file") MultipartFile file) {

        try {

            List<Map<String, String>> excelData =
                    excelUploadService.readExcel(file);

            genericExcelUploadService.process(
                    excelData,
                    attendanceMapper,
                    attendanceRepository
            );

            return ResponseEntity.ok(
                    "Attendance Excel uploaded successfully"
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}