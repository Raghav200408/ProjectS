package com.project.ProjectS.service;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.model.SectionRequestDTO;
import com.project.ProjectS.model.SectionResponseDTO;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.CourseAssignmentRepository;
import com.project.ProjectS.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {
    @Autowired
    public SectionService(SectionRepository sectionRepository, CourseRepository courseRepository, CollegeRepository collegeRepository, BranchRepository branchRepository, CourseAssignmentRepository courseAssignmentRepository, UserRepository userRepository) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.userRepository = userRepository;
    }


    private static final Logger log =
            LogManager.getLogger(SectionService.class);
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final UserRepository userRepository;

    public String create(SectionRequestDTO request, Authentication authentication) {

        User actor = requireActor(authentication);
        requireOrganizationAccess(actor, request.getCollegeId(), request.getBranchId());

        log.info("Creating section with name: {}", request.getSectionName());

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

        if (!branch.getCollege().getCollegeId().equals(college.getCollegeId())) {
            throw new IllegalArgumentException("Selected branch does not belong to the selected college");
        }

        requireCourseAssignment(course, college, branch);

        if (sectionRepository.existsBySectionNameAndCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
                request.getSectionName(), course.getCourseId(), college.getCollegeId(), branch.getBranchId())) {
            throw new RuntimeException("Section already exists for this course and branch");
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

    public List<SectionResponseDTO> getAll(Authentication authentication) {

        log.info("Fetching all sections.");

        User actor = requireActor(authentication);
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        List<Section> visibleSections;
        if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
            visibleSections = sectionRepository.findAll();
        } else if ("COLLEGE_ADMIN".equalsIgnoreCase(role) && actor.getCollege() != null) {
            visibleSections = sectionRepository.findByCollege_CollegeId(actor.getCollege().getCollegeId());
        } else if ("BRANCH_ADMIN".equalsIgnoreCase(role) && actor.getCollege() != null && actor.getBranch() != null
                && actor.getBranch().getCollege().getCollegeId().equals(actor.getCollege().getCollegeId())) {
            visibleSections = sectionRepository.findByBranch_BranchId(actor.getBranch().getBranchId());
        } else {
            throw new AccessDeniedException("You can view sections only in your assigned organization");
        }

        List<SectionResponseDTO> sections = visibleSections
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        log.info("Fetched {} sections.", sections.size());

        return sections;
    }

    public SectionResponseDTO getById(Long id, Authentication authentication) {

        log.info("Fetching section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });
        requireOrganizationAccess(requireActor(authentication), entity.getCollege().getCollegeId(),
                entity.getBranch().getBranchId());

        log.info("Section fetched successfully with ID: {}", id);

        return convert(entity);
    }
    public String update(Long id, SectionRequestDTO request, Authentication authentication) {

        log.info("Updating section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });
        User actor = requireActor(authentication);
        requireOrganizationAccess(actor, entity.getCollege().getCollegeId(), entity.getBranch().getBranchId());
        requireOrganizationAccess(actor, request.getCollegeId(), request.getBranchId());

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

        if (!branch.getCollege().getCollegeId().equals(college.getCollegeId())) {
            throw new IllegalArgumentException("Selected branch does not belong to the selected college");
        }

        requireCourseAssignment(course, college, branch);

        if (sectionRepository.existsBySectionNameAndCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchIdAndSectionIdNot(
                request.getSectionName(), course.getCourseId(), college.getCollegeId(), branch.getBranchId(), id)) {
            throw new RuntimeException("Section already exists for this course and branch");
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

    private void requireCourseAssignment(Course course, College college, Branch branch) {
        boolean assigned = courseAssignmentRepository.findActiveForScope(
                        college.getCollegeId(), branch.getBranchId())
                .stream().anyMatch(assignment ->
                        assignment.getCourse().getCourseId().equals(course.getCourseId()));
        if (!assigned) {
            throw new IllegalArgumentException("Course is not assigned to the selected college and branch");
        }
    }

    public String delete(Long id, Authentication authentication) {

        log.info("Deleting section with ID: {}", id);

        Section entity = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Section not found with ID: {}", id);
                    return new RuntimeException("Section not found");
                });
        requireOrganizationAccess(requireActor(authentication), entity.getCollege().getCollegeId(),
                entity.getBranch().getBranchId());

        sectionRepository.delete(entity);

        log.info("Section deleted successfully with ID: {}", id);

        return "Section deleted successfully";
    }

    public void importSection(Section section, Authentication authentication) {
        requireOrganizationAccess(requireActor(authentication), section.getCollege().getCollegeId(),
                section.getBranch().getBranchId());
        requireCourseAssignment(section.getCourse(), section.getCollege(), section.getBranch());
        if (sectionRepository.existsBySectionNameAndCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
                section.getSectionName(), section.getCourse().getCourseId(), section.getCollege().getCollegeId(),
                section.getBranch().getBranchId())) {
            throw new IllegalArgumentException("Section already exists for this course and branch");
        }
        section.setActiveRow(true);
        sectionRepository.save(section);
    }

    private User requireActor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authenticated admin required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Admin account not found"));
    }

    private void requireOrganizationAccess(User actor, Long collegeId, Long branchId) {
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if ("SUPER_ADMIN".equalsIgnoreCase(role)) return;
        Long actorCollegeId = actor.getCollege() == null ? null : actor.getCollege().getCollegeId();
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role) && actorCollegeId != null && actorCollegeId.equals(collegeId)) return;
        Long actorBranchId = actor.getBranch() == null ? null : actor.getBranch().getBranchId();
        if ("BRANCH_ADMIN".equalsIgnoreCase(role) && actorCollegeId != null && actorCollegeId.equals(collegeId)
                && actorBranchId != null && actorBranchId.equals(branchId)) return;
        throw new AccessDeniedException("You can access sections only in your assigned organization");
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
