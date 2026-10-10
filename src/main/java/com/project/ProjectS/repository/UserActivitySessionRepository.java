package com.project.ProjectS.repository;

import com.project.ProjectS.entity.UserActivitySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserActivitySessionRepository extends JpaRepository<UserActivitySession, Long> {
    Optional<UserActivitySession> findBySessionKeyAndUser_UserId(String sessionKey, Long userId);

    List<UserActivitySession> findByUser_UserIdAndStatusIn(Long userId, Collection<String> statuses);

    @Query("""
            select s from UserActivitySession s
            join s.user u
            where s.status = 'ACTIVE'
              and u.role.roleName = 'STUDENT'
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:studentId is null or u.userId = :studentId)
            """)
    List<UserActivitySession> findActiveStudentSessionsForScope(
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("studentId") Long studentId
    );

    List<UserActivitySession> findByStatusAndLastHeartbeatAtBefore(String status, LocalDateTime cutoff);

    List<UserActivitySession> findByStatusInAndLastInteractionAtBefore(
            Collection<String> statuses,
            LocalDateTime cutoff
    );
}