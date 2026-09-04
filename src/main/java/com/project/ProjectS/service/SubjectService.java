package com.project.ProjectS.service;


import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import com.project.ProjectS.model.SubjectRequestDTO;
import com.project.ProjectS.model.SubjectResponseDTO;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public SubjectService(
            SubjectRepository subjectRepository,
            CourseRepository courseRepository) {

        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
    }

    // CREATE
    public String create(SubjectRequestDTO request) {

        if (subjectRepository.existsBySubjectNameAndCourse_CourseId(
                request.getSubjectName(),
                request.getCourseId())) {

            throw new RuntimeException(
                    "Subject already exists for this course"
            );
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found"));

        Subject subject = new Subject();

        subject.setSubjectName(request.getSubjectName());
        subject.setCourse(course);

        if (request.getActiveRow() != null) {
            subject.setActiveRow(request.getActiveRow());
        } else {
            subject.setActiveRow(true);
        }

        if (request.getRowStatus() != null) {
            subject.setRowStatus(request.getRowStatus());
        } else {
            subject.setRowStatus(1);
        }

        subjectRepository.save(subject);

        return "Subject created successfully";
    }

    // GET ALL
    public List<SubjectResponseDTO> getAll() {

        List<Subject> subjects = subjectRepository.findAll();

        List<SubjectResponseDTO> response = new ArrayList<>();

        for (Subject subject : subjects) {
            response.add(convertToResponse(subject));
        }

        return response;
    }

    // GET BY ID
    public SubjectResponseDTO getById(Long id) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        return convertToResponse(subject);
    }

    // GET ACTIVE SUBJECTS
    public List<SubjectResponseDTO> getActiveSubjects() {

        List<Subject> subjects = subjectRepository.findByActiveRowTrue();

        List<SubjectResponseDTO> response = new ArrayList<>();

        for (Subject subject : subjects) {
            response.add(convertToResponse(subject));
        }

        return response;
    }

    // GET SUBJECTS BY COURSE
    public List<SubjectResponseDTO> getByCourseId(Long courseId) {

        List<Subject> subjects =
                subjectRepository.findByCourse_CourseId(courseId);

        List<SubjectResponseDTO> response = new ArrayList<>();

        for (Subject subject : subjects) {
            response.add(convertToResponse(subject));
        }

        return response;
    }

    // UPDATE
    public String update(Long id, SubjectRequestDTO request) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (subjectRepository
                .existsBySubjectNameAndCourse_CourseIdAndSubjectIdNot(
                        request.getSubjectName(),
                        request.getCourseId(),
                        id)) {

            throw new RuntimeException(
                    "Subject already exists for this course"
            );
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found"));

        subject.setSubjectName(request.getSubjectName());
        subject.setCourse(course);

        if (request.getActiveRow() != null) {
            subject.setActiveRow(request.getActiveRow());
        }

        if (request.getRowStatus() != null) {
            subject.setRowStatus(request.getRowStatus());
        }

        subjectRepository.save(subject);

        return "Subject updated successfully";
    }

    // DELETE
    public String delete(Long id) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        subjectRepository.delete(subject);

        return "Subject deleted successfully";
    }

    // CONVERT ENTITY TO DTO
    private SubjectResponseDTO convertToResponse(Subject subject) {

        SubjectResponseDTO dto = new SubjectResponseDTO();

        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getSubjectName());

        if (subject.getCourse() != null) {
            dto.setCourseId(subject.getCourse().getCourseId());
            dto.setCourseName(subject.getCourse().getName());
        }

        dto.setActiveRow(subject.getActiveRow());
        dto.setRowStatus(subject.getRowStatus());
        dto.setCreatedAt(subject.getCreatedAt());
        dto.setUpdatedAt(subject.getUpdatedAt());

        return dto;
    }
}
