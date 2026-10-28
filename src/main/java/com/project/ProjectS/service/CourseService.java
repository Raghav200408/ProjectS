package com.project.ProjectS.service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.CourseAssignment;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.CourseRequestDTO;
import com.project.ProjectS.model.CourseResponseDTO;
import com.project.ProjectS.model.CourseAssignmentRequestDTO;
import com.project.ProjectS.model.CourseAssignmentResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.CourseAssignmentRepository;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.security.core.Authentication;
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
public class CourseService {
    @Autowired
    public CourseService(CourseRepository courseRepository, BranchRepository branchRepository, CollegeRepository collegeRepository, CourseAssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.branchRepository = branchRepository;
        this.collegeRepository = collegeRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }


    private static final Logger log =
            LogManager.getLogger(CourseService.class);
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final CollegeRepository collegeRepository;
    private final CourseAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @org.springframework.transaction.annotation.Transactional
    public String create(CourseRequestDTO request, Authentication authentication) {
        log.info("Creating catalog course with name: {}", request.getName());
        if (request.getBranchId() != null && request.getCollegeId() == null) {
            request.setCollegeId(branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"))
                    .getCollege().getCollegeId());
        }

        if (request.getCollegeId() != null) validateAssignmentScope(
                request.getCollegeId(), request.getBranchId(), authentication);
        Course entity = new Course();
        entity.setName(request.getName().trim());
        entity.setActiveRow(request.getActiveRow() == null ? true : request.getActiveRow());
        entity.setRowStatus(request.getRowStatus() == null ? 1 : request.getRowStatus());
        Course saved = courseRepository.save(entity);

        if (request.getCollegeId() != null) {
            CourseAssignmentRequestDTO assignment = new CourseAssignmentRequestDTO();
            assignment.setCollegeId(request.getCollegeId());
            assignment.setBranchId(request.getBranchId());
            assignment.setActiveRow(true);
            addAssignment(saved, assignment);
        }
        return "Course created successfully";
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseResponseDTO> getAll(Long collegeId, Long branchId, Authentication authentication) {
        User actor = requireActor(authentication);
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        log.info("Fetching courses collegeId={} branchId={} role={}", collegeId, branchId, role);
        Long actorCollegeId = actor.getCollege() == null ? null : actor.getCollege().getCollegeId();
        Long actorBranchId = actor.getBranch() == null ? null : actor.getBranch().getBranchId();
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role)) {
            if (actorCollegeId == null || (collegeId != null && !collegeId.equals(actorCollegeId))) {
                throw new org.springframework.security.access.AccessDeniedException("You can browse only your college");
            }
            collegeId = actorCollegeId;
        } else if ("BRANCH_ADMIN".equalsIgnoreCase(role)) {
            if (actorCollegeId == null || actorBranchId == null
                    || (collegeId != null && !collegeId.equals(actorCollegeId))
                    || (branchId != null && !branchId.equals(actorBranchId))) {
                throw new org.springframework.security.access.AccessDeniedException("You can browse only your branch");
            }
            collegeId = actorCollegeId;
            branchId = actorBranchId;
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            if (collegeId != null && !collegeId.equals(actorCollegeId)
                    || branchId != null && !branchId.equals(actorBranchId)) {
                throw new org.springframework.security.access.AccessDeniedException("You can browse only courses assigned to your organization");
            }
            collegeId = actorCollegeId;
            branchId = actorBranchId;
        }
        List<Course> source;
        if (branchId != null && collegeId == null) {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            collegeId = branch.getCollege().getCollegeId();
        }
        if ("STUDENT".equalsIgnoreCase(role)) {
            if (collegeId == null) source = List.of();
            else source = (branchId == null
                    ? assignmentRepository.findActiveCollegeWide(collegeId)
                    : assignmentRepository.findActiveForScope(collegeId, branchId))
                    .stream().map(CourseAssignment::getCourse).distinct().toList();
        } else if (collegeId != null) {
            source = assignmentRepository.findActiveForScope(collegeId, branchId).stream()
                    .map(CourseAssignment::getCourse).distinct().toList();
        } else {
            source = courseRepository.findAll();
        }
        List<CourseResponseDTO> courses = source.stream()
                .map(course -> this.convert(course, actor))
                .collect(Collectors.toList());

        log.info("Fetched {} courses.", courses.size());

        return courses;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public CourseResponseDTO getById(Long id, Authentication authentication) {
        User actor = requireActor(authentication);
        log.info("Fetching course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if ("STUDENT".equalsIgnoreCase(role) && !isCourseAssignedToActor(entity, actor)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Course not found");
        }
        log.info("Course fetched successfully with ID: {}", id);
        return convert(entity, actor);
    }

    @org.springframework.transaction.annotation.Transactional
    public String update(Long id, CourseRequestDTO request, Authentication authentication) {
        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        entity.setName(request.getName().trim());
        if (request.getActiveRow() != null) entity.setActiveRow(request.getActiveRow());
        if (request.getRowStatus() != null) entity.setRowStatus(request.getRowStatus());
        Course saved = courseRepository.save(entity);

        // Older clients may still submit a location while editing. Treat it as
        // an additional assignment rather than moving the shared catalog item.
        if (request.getBranchId() != null || request.getCollegeId() != null) {
            if (request.getCollegeId() == null && request.getBranchId() != null) {
                request.setCollegeId(branchRepository.findById(request.getBranchId())
                        .orElseThrow(() -> new RuntimeException("Branch not found"))
                        .getCollege().getCollegeId());
            }
            if (request.getCollegeId() != null) {
                validateAssignmentScope(request.getCollegeId(), request.getBranchId(), authentication);
                CourseAssignmentRequestDTO assignment = new CourseAssignmentRequestDTO();
                assignment.setCollegeId(request.getCollegeId());
                assignment.setBranchId(request.getBranchId());
                assignment.setActiveRow(true);
                addAssignment(saved, assignment);
            }
        }
        return "Course updated successfully";
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CourseAssignmentResponseDTO> getAssignments(Long courseId, Authentication authentication) {
        requireCourse(courseId);
        User actor = requireActor(authentication);
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        List<CourseAssignment> rows = assignmentRepository.findByCourse_CourseId(courseId);
        if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
            return rows.stream().map(this::toAssignmentResponse).toList();
        }
        Long collegeId = actor.getCollege() == null ? null : actor.getCollege().getCollegeId();
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role)) {
            return rows.stream().filter(row -> row.getCollege().getCollegeId().equals(collegeId))
                    .map(this::toAssignmentResponse).toList();
        }
        Long branchId = actor.getBranch() == null ? null : actor.getBranch().getBranchId();
        if ("BRANCH_ADMIN".equalsIgnoreCase(role)) {
            return rows.stream().filter(row -> row.getCollege().getCollegeId().equals(collegeId)
                            && (row.getBranch() == null || row.getBranch().getBranchId().equals(branchId)))
                    .map(this::toAssignmentResponse).toList();
        }
        throw new org.springframework.security.access.AccessDeniedException("Admin role required");
    }

    @org.springframework.transaction.annotation.Transactional
    public List<CourseAssignmentResponseDTO> replaceAssignments(
            Long courseId, List<CourseAssignmentRequestDTO> requests, Authentication authentication) {
        Course course = requireCourse(courseId);
        User actor = requireActor(authentication);
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        List<CourseAssignmentRequestDTO> incoming = requests == null ? List.of() : requests;
        List<CourseAssignmentRequestDTO> merged = new java.util.ArrayList<>();

        if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
            merged.addAll(incoming);
        } else {
            Long collegeId = actor.getCollege() == null ? null : actor.getCollege().getCollegeId();
            if ("COLLEGE_ADMIN".equalsIgnoreCase(role)) {
                if (collegeId == null || incoming.stream().anyMatch(row -> !collegeId.equals(row.getCollegeId()))) {
                    throw new org.springframework.security.access.AccessDeniedException("You can manage assignments only for your college");
                }
                assignmentRepository.findByCourse_CourseId(courseId).stream()
                        .filter(row -> !row.getCollege().getCollegeId().equals(collegeId))
                        .map(this::toRequest)
                        .forEach(merged::add);
                merged.addAll(incoming);
            } else if ("BRANCH_ADMIN".equalsIgnoreCase(role)) {
                Long branchId = actor.getBranch() == null ? null : actor.getBranch().getBranchId();
                if (collegeId == null || branchId == null || incoming.stream().anyMatch(row ->
                        !collegeId.equals(row.getCollegeId()) || row.getBranchId() == null || !branchId.equals(row.getBranchId()))) {
                    throw new org.springframework.security.access.AccessDeniedException("You can manage assignments only for your branch");
                }
                assignmentRepository.findByCourse_CourseId(courseId).stream()
                        .filter(row -> !row.getCollege().getCollegeId().equals(collegeId)
                                || row.getBranch() == null
                                || !row.getBranch().getBranchId().equals(branchId))
                        .map(this::toRequest)
                        .forEach(merged::add);
                merged.addAll(incoming);
            } else {
                throw new org.springframework.security.access.AccessDeniedException("Admin role required");
            }
        }

        validateAssignments(merged);
        assignmentRepository.deleteByCourse_CourseId(courseId);
        merged.forEach(request -> addAssignment(course, request));
        return getAssignments(courseId, authentication);
    }

    private void validateAssignments(List<CourseAssignmentRequestDTO> requests) {
        java.util.Set<String> keys = new java.util.HashSet<>();
        java.util.Set<Long> collegeWide = new java.util.HashSet<>();
        for (CourseAssignmentRequestDTO request : requests) {
            if (request.getCollegeId() == null) throw new IllegalArgumentException("College ID is required");
            String key = request.getCollegeId() + ":" + (request.getBranchId() == null ? "all" : request.getBranchId());
            if (!keys.add(key)) throw new IllegalArgumentException("Duplicate course assignment");
            if (request.getBranchId() == null) collegeWide.add(request.getCollegeId());
            else {
                Branch branch = branchRepository.findById(request.getBranchId())
                        .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
                if (!branch.getCollege().getCollegeId().equals(request.getCollegeId())) {
                    throw new IllegalArgumentException("Selected branch does not belong to the selected college");
                }
            }
        }
        if (requests.stream().anyMatch(request -> request.getBranchId() != null
                && collegeWide.contains(request.getCollegeId()))) {
            throw new IllegalArgumentException("Use either all branches or selected branches for a college, not both");
        }
        for (CourseAssignmentRequestDTO request : requests) {
            if (request.getBranchId() == null && !collegeRepository.existsById(request.getCollegeId())) {
                throw new IllegalArgumentException("College not found");
            }
        }
    }

    private CourseAssignmentRequestDTO toRequest(CourseAssignment assignment) {
        CourseAssignmentRequestDTO request = new CourseAssignmentRequestDTO();
        request.setCollegeId(assignment.getCollege().getCollegeId());
        request.setBranchId(assignment.getBranch() == null ? null : assignment.getBranch().getBranchId());
        request.setActiveRow(assignment.getActiveRow());
        return request;
    }

    private void validateAssignmentScope(Long collegeId, Long branchId, Authentication authentication) {
        User actor = requireActor(authentication);
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if ("SUPER_ADMIN".equalsIgnoreCase(role)) return;
        Long actorCollegeId = actor.getCollege() == null ? null : actor.getCollege().getCollegeId();
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role) && actorCollegeId != null && actorCollegeId.equals(collegeId)) return;
        Long actorBranchId = actor.getBranch() == null ? null : actor.getBranch().getBranchId();
        if ("BRANCH_ADMIN".equalsIgnoreCase(role) && actorCollegeId != null && actorCollegeId.equals(collegeId)
                && actorBranchId != null && actorBranchId.equals(branchId)) return;
        throw new org.springframework.security.access.AccessDeniedException("You cannot assign courses outside your organization scope");
    }

    private User requireActor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authenticated admin required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Admin account not found"));
    }

    private void addAssignment(Course course, CourseAssignmentRequestDTO request) {
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new IllegalArgumentException("College not found"));
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
            if (!branch.getCollege().getCollegeId().equals(college.getCollegeId())) {
                throw new IllegalArgumentException("Selected branch does not belong to the selected college");
            }
        }
        boolean exists = branch == null
                ? assignmentRepository.existsByCourse_CourseIdAndCollege_CollegeIdAndBranchIsNull(
                    course.getCourseId(), college.getCollegeId())
                : assignmentRepository.existsByCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
                    course.getCourseId(), college.getCollegeId(), branch.getBranchId());
        boolean collegeWideRequest = branch == null;
        boolean conflictingScope = assignmentRepository.findByCourse_CourseId(course.getCourseId()).stream()
                .anyMatch(row -> row.getCollege().getCollegeId().equals(college.getCollegeId())
                        && (collegeWideRequest ? row.getBranch() != null : row.getBranch() == null));
        if (!exists && conflictingScope) {
            throw new IllegalArgumentException("Use either an all-branches assignment or selected branch assignments for a college");
        }
        if (exists) return;
        CourseAssignment assignment = new CourseAssignment();
        assignment.setCourse(course);
        assignment.setCollege(college);
        assignment.setBranch(branch);
        assignment.setActiveRow(request.getActiveRow() == null ? true : request.getActiveRow());
        assignmentRepository.save(assignment);
    }

    private Course requireCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
    }

    @org.springframework.transaction.annotation.Transactional
    public String delete(Long id) {

        log.info("Deleting course with ID: {}", id);

        Course entity = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", id);
                    return new RuntimeException("Course not found");
                });

        assignmentRepository.deleteByCourse_CourseId(id);
        courseRepository.delete(entity);

        log.info("Course deleted successfully with ID: {}", id);

        return "Course deleted successfully";
    }

    private CourseResponseDTO convert(Course entity, User actor) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setCourseId(entity.getCourseId());
        dto.setName(entity.getName());
        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        List<CourseAssignment> assignments = assignmentRepository.findByCourse_CourseIdAndActiveRowTrue(entity.getCourseId())
                .stream().filter(row -> assignmentVisibleTo(row, actor)).toList();
        dto.setAssignments(assignments.stream().map(this::toAssignmentResponse).toList());
        if (assignments.size() == 1) {
            CourseAssignment assignment = assignments.get(0);
            dto.setCollegeId(assignment.getCollege().getCollegeId());
            dto.setCollegeName(assignment.getCollege().getInstituteName());
            if (assignment.getBranch() != null) {
                dto.setBranchId(assignment.getBranch().getBranchId());
                dto.setBranchName(assignment.getBranch().getBranchName());
            }
        } else if (assignments.isEmpty()) {
            // Keep legacy data visible until every environment has run the backfill.
            if (entity.getCollege() != null) {
                dto.setCollegeId(entity.getCollege().getCollegeId());
                dto.setCollegeName(entity.getCollege().getInstituteName());
            }
            if (entity.getBranch() != null) {
                dto.setBranchId(entity.getBranch().getBranchId());
                dto.setBranchName(entity.getBranch().getBranchName());
            }
        }
        return dto;
    }

    private boolean assignmentVisibleTo(CourseAssignment row, User actor) {
        String role = actor == null || actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if ("SUPER_ADMIN".equalsIgnoreCase(role) || "GUEST".equalsIgnoreCase(role)) return true;
        Long collegeId = actor.getCollege() == null ? null : actor.getCollege().getCollegeId();
        if (collegeId == null || !row.getCollege().getCollegeId().equals(collegeId)) return false;
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role) || row.getBranch() == null) return true;
        Long branchId = actor.getBranch() == null ? null : actor.getBranch().getBranchId();
        return branchId != null && row.getBranch().getBranchId().equals(branchId);
    }

    private boolean isCourseAssignedToActor(Course course, User actor) {
        return assignmentRepository.findByCourse_CourseIdAndActiveRowTrue(course.getCourseId())
                .stream().anyMatch(row -> assignmentVisibleTo(row, actor));
    }

    private CourseAssignmentResponseDTO toAssignmentResponse(CourseAssignment assignment) {
        CourseAssignmentResponseDTO dto = new CourseAssignmentResponseDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setCollegeId(assignment.getCollege().getCollegeId());
        dto.setCollegeName(assignment.getCollege().getInstituteName());
        dto.setBranchId(assignment.getBranch() == null ? null : assignment.getBranch().getBranchId());
        dto.setBranchName(assignment.getBranch() == null ? null : assignment.getBranch().getBranchName());
        dto.setActiveRow(assignment.getActiveRow());
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
                Long collegeId = branch.getCollege().getCollegeId();
                boolean alreadyAssigned = assignmentRepository.findActiveForScope(collegeId, branch.getBranchId())
                        .stream().anyMatch(assignment -> assignment.getCourse().getName().equalsIgnoreCase(courseName));
                if (alreadyAssigned) {
                    skippedCount++;
                    continue;
                }

                List<Course> sameName = courseRepository.findByNameIgnoreCase(courseName);
                if (sameName.size() > 1) {
                    throw new IllegalArgumentException("Course name is ambiguous in the catalog: " + courseName);
                }
                Course course = sameName.isEmpty() ? new Course() : sameName.get(0);
                if (course.getCourseId() == null) {
                    course.setName(courseName);
                    course.setActiveRow(true);
                    course.setRowStatus(1);
                    course = courseRepository.save(course);
                }
                CourseAssignmentRequestDTO assignment = new CourseAssignmentRequestDTO();
                assignment.setCollegeId(collegeId);
                assignment.setBranchId(branch.getBranchId());
                assignment.setActiveRow(true);
                addAssignment(course, assignment);
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
