package com.project.ProjectS.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.mapper.CourseMapper;
import com.project.ProjectS.model.CourseRequestDTO;
import com.project.ProjectS.model.CourseResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final CourseMapper courseMapper;

    // Create
    public CourseResponseDTO saveCourse(CourseRequestDTO request) {

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        Course course = courseMapper.toEntity(request, branch);

        Course saved = courseRepository.save(course);

        return courseMapper.toResponseDTO(saved);
    }

    // Get All
    public List<CourseResponseDTO> getAllCourses() {

        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get By Id
    public CourseResponseDTO getCourseById(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return courseMapper.toResponseDTO(course);
    }

    // Update
    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO request) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        course.setBranch(branch);
        course.setName(request.getName());
        course.setRowStatus(request.getRowStatus());
        course.setActiveRow(request.getActiveRow());

        Course updated = courseRepository.save(course);

        return courseMapper.toResponseDTO(updated);
    }

    // Delete
    public String deleteCourse(Long id) {

        courseRepository.deleteById(id);

        return "Course deleted successfully.";
    }

}