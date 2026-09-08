package com.project.ProjectS.controller;


import com.project.ProjectS.model.PerformanceDashboardResponseDTO;
import com.project.ProjectS.model.StudentPerformanceDTO;
import com.project.ProjectS.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;

    @Autowired
    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/super-admin")
    public ResponseEntity<PerformanceDashboardResponseDTO> getSuperAdminPerformance(
            Authentication authentication,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long examId) {

        PerformanceDashboardResponseDTO response =
                performanceService.getSuperAdminPerformance(
                        authentication,
                        collegeId,
                        branchId,
                        courseId,
                        sectionId,
                        examId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/college")
    public ResponseEntity<PerformanceDashboardResponseDTO> getCollegePerformance(
            Authentication authentication,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long examId) {

        PerformanceDashboardResponseDTO response =
                performanceService.getCollegePerformance(
                        authentication,
                        branchId,
                        courseId,
                        sectionId,
                        examId
                );

        return ResponseEntity.ok(response);
    }


    /*
     * Branch Admin Performance
     */
    @GetMapping("/branch")
    public ResponseEntity<PerformanceDashboardResponseDTO> getBranchPerformance(
            Authentication authentication,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long examId) {

        PerformanceDashboardResponseDTO response =
                performanceService.getBranchPerformance(
                        authentication,
                        courseId,
                        sectionId,
                        examId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student")
    public ResponseEntity<StudentPerformanceDTO> getStudentPerformance(
            Authentication authentication) {

        StudentPerformanceDTO response =
                performanceService.getStudentPerformance(
                        authentication
                );

        return ResponseEntity.ok(response);
    }
}
