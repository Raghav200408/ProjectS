package com.project.ProjectS.repository;

import com.project.ProjectS.entity.UserSubscription;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findFirstByUser_UserIdAndCourse_CourseIdAndActiveTrue(Long userId, Long courseId);
    List<UserSubscription> findByUser_UserIdAndActiveTrue(Long userId);

    List<UserSubscription> findByPlan_PlanIdAndActiveTrue(Long planId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSubscription s join fetch s.plan join fetch s.course " +
            "where s.user.userId = :userId and s.course.courseId = :courseId " +
            "and s.active = true and (s.expiresAt is null or s.expiresAt > :now)")
    Optional<UserSubscription> findEntitledSubscription(@Param("userId") Long userId,
                                                         @Param("courseId") Long courseId,
                                                         @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"user", "user.branch", "course", "plan"})
    List<UserSubscription> findAllByOrderByStartsAtDesc();

    @EntityGraph(attributePaths = {"user", "user.branch", "course", "plan"})
    List<UserSubscription> findByUser_Branch_BranchIdOrderByStartsAtDesc(Long branchId);
}
