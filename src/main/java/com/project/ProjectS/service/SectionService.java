package com.project.ProjectS.service;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;

import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {
    @Autowired private CourseAssignmentService courseAssignments;
    @Autowired
    public SectionService(SectionRepository sectionRepository, CourseRepository courseRepository, CollegeRepository collegeRepository, BranchRepository branchRepository) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
    }


    private static final Logger log =
            LogManager.getLogger(SectionService.class);
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;

    public String create(SectionRequestDTO request) {

        log.info("Creating section with name: {}", request.getSectionName());

        courseAssignments.requireOrganization(courseAssignments.currentUser(), request.getCollegeId(), request.getBranchId());
        courseAssignments.requireAssignment(request.getCollegeId(), request.getBranchId(), request.getCourseId());
        if (sectionRepository.existsBySectionNameAndCourse_CourseIdAndBranch_BranchId(request.getSectionName(), request.getCourseId(), request.getBranchId()))
            throw new IllegalArgumentException("Section already exists in this branch/course");

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> {

                    log.warn(
                            "College not found with ID: {}",
                            request.getCollegeId()
                    );

                    return new RuntimeException("College not found");
                });

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> {

                    log.warn(
                            "Branch not found with ID: {}",
                            request.getBranchId()
                    );

                    return new RuntimeException("Branch not found");
                });

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {

                    log.warn(
                            "Course not found with ID: {}",
                            request.getCourseId()
                    );

                    return new RuntimeException(
                            "Course not found"
                    );
                });



        Section entity = new Section();


        entity.setCollege(college);
        entity.setBranch(branch);
        entity.setCourse(course);

        entity.setSectionName(
                request.getSectionName()
        );


        entity.setDescription(
                request.getDescription()
        );
        entity.setActiveRow(
                request.getActiveRow() != null
                        ? request.getActiveRow()
                        : true
        );


        sectionRepository.save(entity);



        log.info(
                "Section created successfully with name: {}",
                request.getSectionName()
        );


        return "Section created successfully";
    }

    public List<SectionResponseDTO> getAll() {

        log.info("Fetching all sections.");

        List<SectionResponseDTO> sections = sectionRepository.findAll()
                .stream().filter(section -> {
                    try { courseAssignments.requireOrganization(courseAssignments.currentUser(), section.getCollege().getCollegeId(), section.getBranch().getBranchId()); return true; }
                    catch (org.springframework.security.access.AccessDeniedException denied) { return false; }
                })
                .map(this::convert)
                .collect(Collectors.toList());

        log.info("Fetched {} sections.", sections.size());

        return sections;
    }

    public SectionResponseDTO getById(Long id) {

        log.info("Fetching section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });

        log.info("Section fetched successfully with ID: {}", id);

        courseAssignments.requireOrganization(courseAssignments.currentUser(), entity.getCollege().getCollegeId(), entity.getBranch().getBranchId());
        return convert(entity);
    }
    public String update(Long id, SectionRequestDTO request) {

        log.info("Updating section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> {

                    log.warn(
                            "College not found with ID: {}",
                            request.getCollegeId()
                    );

                    return new RuntimeException("College not found");
                });

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> {

                    log.warn(
                            "Branch not found with ID: {}",
                            request.getBranchId()
                    );

                    return new RuntimeException("Branch not found");
                });

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", request.getCourseId());
                    return new RuntimeException("Course not found");
                });

        courseAssignments.requireOrganization(courseAssignments.currentUser(), entity.getCollege().getCollegeId(), entity.getBranch().getBranchId());
        courseAssignments.requireOrganization(courseAssignments.currentUser(), request.getCollegeId(), request.getBranchId());
        courseAssignments.requireAssignment(request.getCollegeId(), request.getBranchId(), request.getCourseId());
        if (sectionRepository.existsBySectionNameAndCourse_CourseIdAndBranch_BranchIdAndSectionIdNot(request.getSectionName(), request.getCourseId(), request.getBranchId(), id))
            throw new IllegalArgumentException("Section already exists in this branch/course");

        entity.setCollege(college);
        entity.setBranch(branch);
        entity.setCourse(course);
        entity.setSectionName(request.getSectionName());
        entity.setDescription(request.getDescription());
        entity.setActiveRow(
                request.getActiveRow() != null
                        ? request.getActiveRow()
                        : entity.getActiveRow()
        );

        sectionRepository.save(entity);

        log.info("Section updated successfully with ID: {}", id);

        return "Section updated successfully";
    }

    public String delete(Long id) {

        log.info("Deleting section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });

        courseAssignments.requireOrganization(courseAssignments.currentUser(), entity.getCollege().getCollegeId(), entity.getBranch().getBranchId());
        sectionRepository.delete(entity);

        log.info("Section deleted successfully with ID: {}", id);

        return "Section deleted successfully";
    }
    private SectionResponseDTO convert(Section entity) {

        SectionResponseDTO dto = new SectionResponseDTO();

        dto.setSectionId(entity.getSectionId());

        dto.setCollegeId(entity.getCollege().getCollegeId());
        dto.setCollegeName(entity.getCollege().getInstituteName());

        dto.setBranchId(entity.getBranch().getBranchId());
        dto.setBranchName(entity.getBranch().getBranchName());

        dto.setCourseId(entity.getCourse().getCourseId());
        dto.setCourseName(entity.getCourse().getName());

        dto.setSectionName(entity.getSectionName());
        dto.setDescription(entity.getDescription());

        dto.setActiveRow(entity.getActiveRow());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
