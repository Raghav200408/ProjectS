package com.project.ProjectS.service;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import com.project.ProjectS.model.SubjectRequestDTO;
import com.project.ProjectS.model.SubjectResponseDTO;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    public SubjectService(
            SubjectRepository subjectRepository,
            CourseRepository courseRepository) {
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
    }

    public SubjectResponseDTO create(SubjectRequestDTO request) {

        Course course = course(request.getCourseId());

        if (subjectRepository.existsBySubjectNameAndCourse(request.getSubjectName(), course)) {
            throw new RuntimeException("Subject already exists for this course");
        }

        Subject subject = new Subject();

        apply(subject, request, course);

        return toDto(subjectRepository.save(subject));
    }

    public List<SubjectResponseDTO> getAll() {
        return subjectRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public SubjectResponseDTO getById(Long id) {
        return toDto(subject(id));
    }

    public SubjectResponseDTO update(Long id, SubjectRequestDTO request) {
        Subject subject = subject(id);
        Course course = course(request.getCourseId());

        boolean nameOrCourseChanged =
                !subject.getSubjectName().equals(request.getSubjectName())
                        || !subject.getCourse().getCourseId().equals(course.getCourseId());

        if (nameOrCourseChanged
                && subjectRepository.existsBySubjectNameAndCourse(request.getSubjectName(), course)) {
            throw new RuntimeException("Subject already exists for this course");
        }

        apply(subject, request, course);

        return toDto(subjectRepository.save(subject));
    }

    public void delete(Long id) {
        subjectRepository.delete(subject(id));
    }

    private Subject subject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    private Course course(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    private void apply(
            Subject subject,
            SubjectRequestDTO request,
            Course course) {

        subject.setCourse(course);
        subject.setSubjectName(request.getSubjectName());
        subject.setActiveRow(
                request.getActiveRow() == null
                        ? true
                        : request.getActiveRow()
        );
    }

    private SubjectResponseDTO toDto(Subject subject) {
        SubjectResponseDTO dto = new SubjectResponseDTO();

        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setCourseId(subject.getCourse().getCourseId());
        dto.setCourseName(subject.getCourse().getName());
        dto.setActiveRow(subject.getActiveRow());
        dto.setRowStatus(subject.getRowStatus());
        dto.setCreatedAt(subject.getCreatedAt());
        dto.setUpdatedAt(subject.getUpdatedAt());

        return dto;
    }
}
