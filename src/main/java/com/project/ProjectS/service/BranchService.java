package com.project.ProjectS.service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.model.BranchRequestDTO;
import com.project.ProjectS.model.BranchResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;
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


    public BranchService(
            BranchRepository branchRepository,
            CollegeRepository collegeRepository
    ) {
        this.branchRepository = branchRepository;
        this.collegeRepository = collegeRepository;
    }

    public String create(BranchRequestDTO request) {

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

    public List<BranchResponseDTO> getAll() {

        log.info("Fetching all branches.");

        List<BranchResponseDTO> branches = branchRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        log.info("Fetched {} branches.", branches.size());

        return branches;
    }

    public Branch getById(Long id) {

        log.info("Fetching branch with ID: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });

        log.info("Branch fetched successfully with ID: {}", id);

        return branch;
    }

    public String update(Long id, BranchRequestDTO request) {

        log.info("Updating branch with ID: {}", id);

        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> {
                    log.warn("College not found with ID: {}", request.getCollegeId());
                    return new RuntimeException("College not found");
                });

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

    public String delete(Long id) {

        log.info("Deleting branch with ID: {}", id);

        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });

        branchRepository.delete(entity);

        log.info("Branch deleted successfully with ID: {}", id);

        return "Branch deleted successfully";
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