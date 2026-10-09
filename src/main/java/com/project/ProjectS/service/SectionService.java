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

        if (sectionRepository.existsBySectionNameAndCourse_CourseId(
                request.getSectionName(),
                request.getCourseId())) {

            log.warn(
                    "Section {} already exists for course {}",
                    request.getSectionName(),
                    request.getCourseId()
            );

            throw new RuntimeException("Section already exists for this course");
        }

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



        if (sectionRepository.existsBySectionNameAndCourse(
                request.getSectionName(),
                course
        )) {

            log.warn(
                    "Section already exists: {} for course: {}",
                    request.getSectionName(),
                    course.getName()
            );

            throw new RuntimeException(
                    "Section already exists for this course"
            );
        }



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
                .stream()
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

        if ((!entity.getSectionName().equals(request.getSectionName())) ||
                (!entity.getCourse().getCourseId().equals(request.getCourseId()))) {

            if (sectionRepository.existsBySectionNameAndCourse_CourseId(
                    request.getSectionName(),
                    request.getCourseId())) {

                log.warn(
                        "Section {} already exists for course {}",
                        request.getSectionName(),
                        request.getCourseId()
                );

                throw new RuntimeException("Section already exists for this course");
            }
        }

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
