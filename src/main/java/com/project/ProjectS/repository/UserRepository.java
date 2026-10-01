package com.project.ProjectS.repository;

import com.project.ProjectS.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<User> findByRole_RoleNameAndActiveRowTrue(String roleName);

    List<User> findByRole_RoleNameAndBranch_BranchIdAndActiveRowTrue(
            String roleName,
            Long branchId
    );

    boolean existsByPhoneNumberAndUserIdNot(String phoneNumber, Long userId);

    @Query("""
            select u from User u
            where u.activeRow = true and (
                :recipientType = 'ALL'
                or (:recipientType = 'USER' and u.userId = :recipientUserId)
                or (:recipientType = 'ROLE' and u.role.roleName = :recipientRole)
                or (:recipientType = 'COLLEGE' and u.college.collegeId = :collegeId)
                or (:recipientType = 'BRANCH' and u.branch.branchId = :branchId)
                or (:recipientType = 'COURSE' and u.course.courseId = :courseId)
                or (:recipientType = 'SECTION' and u.section.sectionId = :sectionId)
            )
            """)
    List<User> findNotificationRecipients(
            @Param("recipientType") String recipientType,
            @Param("recipientUserId") Long recipientUserId,
            @Param("recipientRole") String recipientRole,
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("courseId") Long courseId,
            @Param("sectionId") Long sectionId);
}
