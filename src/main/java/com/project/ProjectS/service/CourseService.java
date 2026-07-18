package com.project.ProjectS.service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.model.CourseRequestDTO;
import com.project.ProjectS.model.CourseResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BranchRepository branchRepository;

    public String create(CourseRequestDTO request) {

        if (courseRepository.existsByName(request.getName())) {
            throw new RuntimeException("Course already exists");
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        Course entity = new Course();

        entity.setBranch(branch);
        entity.setName(request.getName());

        courseRepository.save(entity);

        return "Course created successfully";
    }

    public List<CourseResponseDTO> getAll() {

        return courseRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public CourseResponseDTO getById(Long id) {

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return convert(entity);
    }

    public String update(Long id, CourseRequestDTO request) {

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        entity.setBranch(branch);
        entity.setName(request.getName());

        courseRepository.save(entity);

        return "Course updated successfully";
    }

    public String delete(Long id) {

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        courseRepository.delete(entity);

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