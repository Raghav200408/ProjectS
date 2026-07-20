package com.project.ProjectS.service;

import com.project.ProjectS.entity.Role;
import com.project.ProjectS.model.RoleResponseDTO;
import com.project.ProjectS.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository repository;

    public List<RoleResponseDTO> getAll() {

        List<Role> roles = repository.findAll();
        List<RoleResponseDTO> response = new ArrayList<>();

        for (Role role : roles) {

            RoleResponseDTO dto = new RoleResponseDTO();

            dto.setRoleId(role.getRoleId());
            dto.setRoleName(role.getRoleName());
            dto.setCreatedAt(role.getCreatedAt());
            dto.setUpdatedAt(role.getUpdatedAt());

            response.add(dto);
        }

        return response;
    }

    public RoleResponseDTO getById(Integer id) {

        Role role = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        RoleResponseDTO dto = new RoleResponseDTO();

        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());


        return dto;
    }
}