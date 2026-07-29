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

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final Logger logger =
            LogManager.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BranchRepository branchRepository;

    public String create(CourseRequestDTO request) {

        logger.info("Creating course with name: {}", request.getName());


        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> {

                    logger.warn(
                            "Branch not found with ID: {}",
                            request.getBranchId()
                    );

                    return new RuntimeException("Branch not found");
                });



        if (courseRepository.existsByNameAndBranch(
                request.getName(),
                branch
        )) {

            logger.warn(
                    "Course already exists: {} in branch {}",
                    request.getName(),
                    branch.getBranchName()
            );

            throw new RuntimeException("Course already exists");
        }



        Course entity = new Course();

        entity.setBranch(branch);
        entity.setName(request.getName());

        courseRepository.save(entity);


        logger.info(
                "Course created successfully with name: {}",
                request.getName()
        );


        return "Course created successfully";
    }

    public List<CourseResponseDTO> getAll() {

        logger.info("Fetching all courses.");

        List<CourseResponseDTO> courses = courseRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        logger.info("Fetched {} courses.", courses.size());

        return courses;
    }

    public CourseResponseDTO getById(Long id) {

        logger.info("Fetching course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        logger.info("Course fetched successfully with ID: {}", id);

        return convert(entity);
    }

    public String update(Long id, CourseRequestDTO request) {

        logger.info("Updating course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> {
                    logger.warn("Branch not found with ID: {}", request.getBranchId());
                    return new RuntimeException("Branch not found");
                });

        entity.setBranch(branch);
        entity.setName(request.getName());

        courseRepository.save(entity);

        logger.info("Course updated successfully with ID: {}", id);

        return "Course updated successfully";
    }

    public String delete(Long id) {

        logger.info("Deleting course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        courseRepository.delete(entity);

        logger.info("Course deleted successfully with ID: {}", id);

        return "Course deleted successfully";
    }

    private CourseResponseDTO convert(Course entity) {

        CourseResponseDTO dto = new CourseResponseDTO();

        dto.setCourseId(entity.getCourseId());

        dto.setBranchId(entity.getBranch().getBranchId());
        dto.setBranchName(entity.getBranch().getBranchName());

        dto.setName(entity.getName());

        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public String uploadCourse(MultipartFile file) {

        logger.info("Starting course Excel upload process.");

        if (file.isEmpty()) {
            logger.warn("Uploaded file is empty.");
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



                logger.info(
                        "Processing course: {} for branch: {}",
                        courseName,
                        branchName
                );



                // Find branch

                List<Branch> branches =
                        branchRepository.findByBranchName(branchName);


                if (branches.isEmpty()) {

                    logger.warn(
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


                    logger.warn(
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


                savedCount++;


                logger.info(
                        "Course saved successfully: {}",
                        courseName
                );

            }



            logger.info(
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

            logger.error(
                    "Failed while processing course Excel file",
                    e
            );

            throw new RuntimeException(
                    "Failed to upload Excel file"
            );
        }
    }
}