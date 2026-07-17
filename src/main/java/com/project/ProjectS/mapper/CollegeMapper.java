package com.project.ProjectS.mapper;

import org.springframework.stereotype.Component;

import com.project.ProjectS.entity.College;

@Component
public class CollegeMapper {

    public College toEntity(com.project.ProjectS.model.CollegeRequestDTO request) {

        return College.builder()
                .instituteName(request.getInstituteName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .activeRow(request.getActiveRow())
                .build();
    }

    public com.project.ProjectS.model.CollegeResponseDTO toResponse(College college) {

        return com.project.ProjectS.model.CollegeResponseDTO.builder()
                .collegeId(college.getCollegeId())
                .instituteName(college.getInstituteName())
                .address(college.getAddress())
                .phoneNumber(college.getPhoneNumber())
                .email(college.getEmail())
                .activeRow(college.getActiveRow())
                .createdAt(college.getCreatedAt())
                .updatedAt(college.getUpdatedAt())
                .build();
    }

}