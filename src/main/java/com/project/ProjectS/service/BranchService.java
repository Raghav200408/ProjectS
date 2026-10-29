package com.project.ProjectS.service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.BranchRequestDTO;
import com.project.ProjectS.model.BranchResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;
import com.project.ProjectS.repository.CourseAssignmentRepository;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private static final Logger log =
            LogManager.getLogger(BranchService.class);

    private final BranchRepository branchRepository;

    private final CollegeRepository collegeRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final UserRepository userRepository;


    public BranchService(
            BranchRepository branchRepository,
            CollegeRepository collegeRepository,
            CourseAssignmentRepository courseAssignmentRepository,
            UserRepository userRepository
    ) {
        this.branchRepository = branchRepository;
        this.collegeRepository = collegeRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.userRepository = userRepository;
    }

    public String create(BranchRequestDTO request, Authentication authentication) {

        User actor = requireActor(authentication);
        requireCollegeAccess(actor, request.getCollegeId());

        log.info("Creating branch: {}", request.getBranchName());


        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> {
                    log.error("College not found with ID: {}", request.getCollegeId());
                    return new RuntimeException("College not found");
                });



        if (branchRepository.existsByBranchNameAndCollege(
                request.getBranchName(),
                college)) {

            log.warn(
                    "Branch already exists: {} under college: {}",
                    request.getBranchName(),
                    college.getInstituteName()
            );

            throw new RuntimeException(
                    "Branch already exists for this college"
            );
        }



        Branch branch = new Branch();

        branch.setCollege(college);
        branch.setBranchName(request.getBranchName());
        branch.setAddress(request.getAddress());
        branch.setPhoneNumber(request.getPhoneNumber());
        branch.setEmail(request.getEmail());
        branch.setActiveRow(
                request.getActiveRow() != null
                        ? request.getActiveRow()
                        : true
        );

        branchRepository.save(branch);


        log.info(
                "Branch created successfully: {}",
                request.getBranchName()
        );


        return "Branch created successfully";
    }

    public List<BranchResponseDTO> getAll(Authentication authentication) {

        log.info("Fetching all branches.");

        User actor = requireActor(authentication);
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        List<Branch> visibleBranches;
        if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
            visibleBranches = branchRepository.findAll();
        } else if ("COLLEGE_ADMIN".equalsIgnoreCase(role) && actor.getCollege() != null) {
            visibleBranches = branchRepository.findByCollege_CollegeId(actor.getCollege().getCollegeId());
        } else {
            throw new AccessDeniedException("You can view branches only in your college");
        }

        List<BranchResponseDTO> branches = visibleBranches
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        log.info("Fetched {} branches.", branches.size());

        return branches;
    }

    public Branch getById(Long id, Authentication authentication) {

        log.info("Fetching branch with ID: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });
        requireCollegeAccess(requireActor(authentication), branch.getCollege().getCollegeId());

        log.info("Branch fetched successfully with ID: {}", id);

        return branch;
    }

    @org.springframework.transaction.annotation.Transactional
    public String update(Long id, BranchRequestDTO request, Authentication authentication) {

        log.info("Updating branch with ID: {}", id);

        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });
        User actor = requireActor(authentication);
        requireCollegeAccess(actor, entity.getCollege().getCollegeId());
        requireCollegeAccess(actor, request.getCollegeId());

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> {
                    log.warn("College not found with ID: {}", request.getCollegeId());
                    return new RuntimeException("College not found");
                });

        for (var assignment : courseAssignmentRepository.findByBranch_BranchId(id)) {
            if (!assignment.getCollege().getCollegeId().equals(college.getCollegeId())) {
                boolean duplicate = courseAssignmentRepository
                        .existsByCourse_CourseIdAndCollege_CollegeIdAndBranch_BranchId(
                                assignment.getCourse().getCourseId(), college.getCollegeId(), id);
                if (duplicate) {
                    throw new IllegalArgumentException("This branch already has a duplicate course assignment in the selected college");
                }
                assignment.setCollege(college);
                courseAssignmentRepository.save(assignment);
            }
        }
        entity.setCollege(college);
        entity.setBranchName(request.getBranchName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());
        entity.setActiveRow(
                request.getActiveRow() != null
                        ? request.getActiveRow()
                        : true
        );
        branchRepository.save(entity);

        log.info("Branch updated successfully with ID: {}", id);

        return "Branch updated successfully";
    }

    public String delete(Long id, Authentication authentication) {

        log.info("Deleting branch with ID: {}", id);

        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });
        requireCollegeAccess(requireActor(authentication), entity.getCollege().getCollegeId());

        branchRepository.delete(entity);

        log.info("Branch deleted successfully with ID: {}", id);

        return "Branch deleted successfully";
    }

    public void authorizeExcelUpload(Authentication authentication) {
        User actor = requireActor(authentication);
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if (!"SUPER_ADMIN".equalsIgnoreCase(role)
                && !("COLLEGE_ADMIN".equalsIgnoreCase(role) && actor.getCollege() != null)) {
            throw new AccessDeniedException("You cannot upload branches outside your college");
        }
    }

    public void importBranch(Branch branch, Authentication authentication) {
        requireCollegeAccess(requireActor(authentication), branch.getCollege().getCollegeId());
        if (branchRepository.existsByBranchNameAndCollege(branch.getBranchName(), branch.getCollege())) {
            throw new IllegalArgumentException("Branch already exists for this college: " + branch.getBranchName());
        }
        branch.setActiveRow(true);
        branchRepository.save(branch);
    }

    private User requireActor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authenticated admin required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Admin account not found"));
    }

    private void requireCollegeAccess(User actor, Long collegeId) {
        String role = actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if ("SUPER_ADMIN".equalsIgnoreCase(role)) return;
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role) && actor.getCollege() != null
                && actor.getCollege().getCollegeId().equals(collegeId)) return;
        throw new AccessDeniedException("You can manage branches only in your assigned college");
    }

    private BranchResponseDTO convert(Branch entity) {

        BranchResponseDTO dto = new BranchResponseDTO();

        dto.setBranchId(entity.getBranchId());

        dto.setCollegeId(entity.getCollege().getCollegeId());
        dto.setCollegeName(entity.getCollege().getInstituteName());

        dto.setBranchName(entity.getBranchName());
        dto.setAddress(entity.getAddress());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());

        dto.setActiveRow(entity.getActiveRow());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
