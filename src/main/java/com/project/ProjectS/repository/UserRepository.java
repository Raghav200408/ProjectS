package com.project.ProjectS.repository;

import com.project.ProjectS.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);


    Optional<User> findByGoogleId(String googleId);

    List<User> findByRole_RoleName(String roleName);
    List<User> findByRole_RoleNameAndBranch_BranchId(
            String roleName,
            Long branchId
    );
    Optional<User> findByUserIdAndRole_RoleName(
            Long userId,
            String roleName
    );
    List<User> findByRole_RoleNameIn(List<String> roleNames);
    boolean existsByEmailAndUserIdNot(String email, Long userId);

    boolean existsByPhoneNumberAndUserIdNot(String phoneNumber, Long userId);
}