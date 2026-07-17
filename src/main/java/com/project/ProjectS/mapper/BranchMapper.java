package com.project.ProjectS.mapper;

import org.springframework.stereotype.Component;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.model.BranchResponseDTO;

@Component
public class BranchMapper {

    public BranchResponseDTO toResponseDTO(Branch branch) {

        return BranchResponseDTO.builder()
                .branchId(branch.getBranchId())
                .collegeId(branch.getCollege().getCollegeId())
                .collegeName(branch.getCollege().getInstituteName())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .phoneNumber(branch.getPhoneNumber())
                .email(branch.getEmail())
                .activeRow(branch.getActiveRow())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

}