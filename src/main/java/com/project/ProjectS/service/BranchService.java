package com.project.ProjectS.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.mapper.BranchMapper;
import com.project.ProjectS.model.BranchRequestDTO;
import com.project.ProjectS.model.BranchResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final CollegeRepository collegeRepository;
    private final BranchMapper branchMapper;

    // Create
    public BranchResponseDTO saveBranch(BranchRequestDTO request) {

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        Branch branch = Branch.builder()
                .college(college)
                .branchName(request.getBranchName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .activeRow(request.getActiveRow())
                .build();

        Branch saved = branchRepository.save(branch);

        return branchMapper.toResponseDTO(saved);
    }

    // Get All
    public List<BranchResponseDTO> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(branchMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get By Id
    public BranchResponseDTO getBranchById(Long id) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        return branchMapper.toResponseDTO(branch);
    }

    // Update
    public BranchResponseDTO updateBranch(Long id, BranchRequestDTO request) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new RuntimeException("College not found"));

        branch.setCollege(college);
        branch.setBranchName(request.getBranchName());
        branch.setAddress(request.getAddress());
        branch.setPhoneNumber(request.getPhoneNumber());
        branch.setEmail(request.getEmail());
        branch.setActiveRow(request.getActiveRow());

        Branch updated = branchRepository.save(branch);

        return branchMapper.toResponseDTO(updated);
    }

    // Delete
    public String deleteBranch(Long id) {

        branchRepository.deleteById(id);

        return "Branch deleted successfully.";
    }

}