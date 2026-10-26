package com.project.ProjectS.service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.model.CourseRequestDTO;
import com.project.ProjectS.model.CourseResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.project.ProjectS.entity.College;
import com.project.ProjectS.repository.CollegeRepository;

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

import java.util.List;
import java.util.stream.Collectors;

@Service
@org.springframework.transaction.annotation.Transactional
public class CourseService {
    @Autowired
    public CourseService(CourseRepository courseRepository, BranchRepository branchRepository, CollegeRepository collegeRepository) {
        this.courseRepository = courseRepository;
        this.branchRepository = branchRepository;
        this.collegeRepository = collegeRepository;
    }


    private static final Logger log =
            LogManager.getLogger(CourseService.class);
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final CollegeRepository collegeRepository;
    @Autowired private CourseAssignmentService assignmentService;
    public List<CourseResponseDTO> getCatalog() {
        if (!CourseAssignmentService.isManager(assignmentService.currentUser()))
            throw new org.springframework.security.access.AccessDeniedException("Administrator access required");
        return courseRepository.findAll().stream().map(this::convert).toList();
    }

    @org.springframework.transaction.annotation.Transactional
    public String create(CourseRequestDTO request) {
        if (!courseRepository.findByNameIgnoreCase(request.getName().trim()).isEmpty())
            throw new IllegalArgumentException("A course with this name already exists; assign the existing course to branches instead");
        Course entity = new Course();
        entity.setName(request.getName().trim());
        entity.setActiveRow(request.getActiveRow() == null ? true : request.getActiveRow());
        entity.setRowStatus(request.getRowStatus() == null ? 1 : request.getRowStatus());
        Branch initialBranch = null;
        if (request.getBranchId() != null || request.getCollegeId() != null) {
            if (request.getBranchId() == null || request.getCollegeId() == null)
                throw new IllegalArgumentException("Supply both college and branch for an initial assignment");
            initialBranch = branchRepository.findById(request.getBranchId()).orElseThrow(() -> new IllegalArgumentException("Branch not found"));
            if (initialBranch.getCollege() == null || !initialBranch.getCollege().getCollegeId().equals(request.getCollegeId()))
                throw new IllegalArgumentException("Branch does not belong to the college");
        }
        courseRepository.save(entity);
        if (initialBranch != null) assignmentService.saveAssignment(entity, initialBranch, true);
        return "Course created successfully";
    }

    public List<CourseResponseDTO> getAll() {

        log.info("Fetching all courses.");

        var caller = assignmentService.currentUser();
        List<CourseResponseDTO> courses = courseRepository.findAll()
                .stream().filter(course -> assignmentService.eligible(caller, course))
                .map(this::convert)
                .collect(Collectors.toList());

        log.info("Fetched {} courses.", courses.size());

        return courses;
    }

    public CourseResponseDTO getById(Long id) {

        log.info("Fetching course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        log.info("Course fetched successfully with ID: {}", id);

        assignmentService.requireEligible(assignmentService.currentUser(), entity);
        return convert(entity);
    }

    public String update(Long id, CourseRequestDTO request) {

        log.info("Updating course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        // Editing shared content never moves its institution assignments.
        entity.setName(request.getName());
        entity.setActiveRow(
                request.getActiveRow() != null
                        ? request.getActiveRow()
                        : entity.getActiveRow()
        );

        entity.setRowStatus(
                request.getRowStatus() != null
                        ? request.getRowStatus()
                        : entity.getRowStatus()
        );

        courseRepository.save(entity);

        log.info("Course updated successfully with ID: {}", id);

        return "Course updated successfully";
    }

    public String delete(Long id) {

        log.info("Deleting course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        entity.setActiveRow(false);
        courseRepository.save(entity);

        log.info("Course deleted successfully with ID: {}", id);

        return "Course deleted successfully";
    }

    private CourseResponseDTO convert(Course entity) {

        CourseResponseDTO dto = new CourseResponseDTO();

        dto.setCourseId(entity.getCourseId());

        if (entity.getCollege() != null) {
            dto.setCollegeId(entity.getCollege().getCollegeId());
            dto.setCollegeName(entity.getCollege().getInstituteName());
        }

        if (entity.getBranch() != null) {
            dto.setBranchId(entity.getBranch().getBranchId());
            dto.setBranchName(entity.getBranch().getBranchName());
        }
        dto.setAssignments(assignmentService.list(entity.getCourseId(), assignmentService.currentUser()));

        dto.setName(entity.getName());

        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public String uploadCourse(MultipartFile file) {

        log.info("Starting course Excel upload process.");

        if (file.isEmpty()) {
            log.warn("Uploaded file is empty.");
            throw new RuntimeException("File cannot be empty");
        }


        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {


            Sheet sheet = workbook.getSheetAt(0);

            boolean headerRow = true;

            int savedCount = 0;
            int skippedCount = 0;


            for (Row row : sheet) {


                // Skip header
                if (headerRow) {
                    headerRow = false;
                    continue;
                }


                String branchName =
                        row.getCell(0)
                                .getStringCellValue()
                                .trim();


                String courseName =
                        row.getCell(1)
                                .getStringCellValue()
                                .trim();



                log.info(
                        "Processing course: {} for branch: {}",
                        courseName,
                        branchName
                );



                // Find branch

                List<Branch> branches =
                        branchRepository.findByBranchName(branchName);


                if (branches.isEmpty()) {

                    log.warn(
                            "Branch not found: {}",
                            branchName
                    );

                    throw new RuntimeException(
                            "Branch not found: " + branchName
                    );
                }


                Branch branch = branches.get(0);


                // Duplicate check

                if(courseRepository.existsByNameAndBranch(
                        courseName,
                        branch
                )){


                    log.warn(
                            "Course already exists: {} in branch {}",
                            courseName,
                            branchName
                    );


                    skippedCount++;

                    continue;
                }



                Course course = new Course();


                course.setBranch(branch);

                course.setName(courseName);


                courseRepository.save(course);
                assignmentService.saveAssignment(course, branch, true);


                savedCount++;


                log.info(
                        "Course saved successfully: {}",
                        courseName
                );

            }



            log.info(
                    "Course Excel upload completed. Saved: {}, Skipped: {}",
                    savedCount,
                    skippedCount
            );


            return "Course Excel upload completed. Saved: "
                    + savedCount
                    + ", Skipped: "
                    + skippedCount;


        }
        catch(Exception e){

            log.error(
                    "Failed while processing course Excel file",
                    e
            );

            throw new RuntimeException(
                    "Failed to upload Excel file"
            );
        }
    }
}
