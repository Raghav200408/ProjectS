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

        if (courseRepository.existsByName(request.getName())) {
            logger.warn("Course already exists with name: {}", request.getName());
            throw new RuntimeException("Course already exists");
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> {
                    logger.warn("Branch not found with ID: {}", request.getBranchId());
                    return new RuntimeException("Branch not found");
                });

        Course entity = new Course();

        entity.setBranch(branch);
        entity.setName(request.getName());

        courseRepository.save(entity);

        logger.info("Course created successfully with name: {}", request.getName());

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
}