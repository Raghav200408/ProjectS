package com.project.ProjectS.repository;

import com.project.ProjectS.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByUserIdForUpdate(@Param("userId") Long userId);

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
            where u.role.roleName = 'STUDENT'
              and (u.activeRow = true or u.activeRow is null)
              and u.lastLoginAt < :cutoff
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:sectionId is null or u.section.sectionId = :sectionId)
            """)
    Page<User> findStudentsNotLoggedInSince(
            @Param("cutoff") java.time.LocalDateTime cutoff,
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("sectionId") Long sectionId,
            Pageable pageable);

    @Query("""
            select count(u.userId) from User u
            where u.role.roleName = 'STUDENT'
              and (u.activeRow = true or u.activeRow is null)
              and u.lastLoginAt < :cutoff
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:sectionId is null or u.section.sectionId = :sectionId)
            """)
    long countStudentsNotLoggedInSince(
            @Param("cutoff") java.time.LocalDateTime cutoff,
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("sectionId") Long sectionId);

    @Query("""
            select count(u.userId) from User u
            where u.role.roleName = 'STUDENT'
              and (u.activeRow = true or u.activeRow is null)
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:sectionId is null or u.section.sectionId = :sectionId)
              and (:courseId is null or u.course.courseId = :courseId)
            """)
    long countActiveStudentsForExamScope(
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("sectionId") Long sectionId,
            @Param("courseId") Long courseId);

    @Query("""
            select count(u.userId) from User u
            where u.role.roleName = 'STUDENT'
              and (u.activeRow = true or u.activeRow is null)
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:sectionId is null or u.section.sectionId = :sectionId)
            """)
    long countActiveStudentsInScope(
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("sectionId") Long sectionId);

    @Query("""
            select count(u.userId) from User u
            where u.role.roleName = 'STUDENT'
              and (u.activeRow = true or u.activeRow is null)
              and u.lastLoginAt is null
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:sectionId is null or u.section.sectionId = :sectionId)
            """)
    long countStudentsWithoutLoginRecord(
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("sectionId") Long sectionId);

    @Query("""
            select u from User u
            left join u.role r
            left join u.college college
            left join u.branch branch
            left join u.course course
            left join u.section section
            where u.activeRow = true and (
                :recipientType = 'ALL'
                or (:recipientType = 'USER' and u.userId = :recipientUserId)
                or (:recipientType = 'ROLE' and r.roleName = :recipientRole)
                or (:recipientType = 'COLLEGE' and college.collegeId = :collegeId)
                or (:recipientType = 'BRANCH' and branch.branchId = :branchId)
                or (:recipientType = 'COURSE' and course.courseId = :courseId)
                or (:recipientType = 'SECTION' and section.sectionId = :sectionId)
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
