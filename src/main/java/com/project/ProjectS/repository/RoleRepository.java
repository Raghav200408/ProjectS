package com.project.ProjectS.repository;

import com.project.ProjectS.entity.Role;
import com.project.ProjectS.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}