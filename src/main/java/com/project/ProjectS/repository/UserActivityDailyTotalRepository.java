package com.project.ProjectS.repository;

import com.project.ProjectS.entity.UserActivityDailyTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserActivityDailyTotalRepository extends JpaRepository<UserActivityDailyTotal, Long> {

    interface DailyTotalProjection {
        LocalDate getActivityDate();

        Long getActiveSeconds();
    }

    List<UserActivityDailyTotal> findByUser_UserIdAndActivityDateBetweenOrderByActivityDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
            select d.activityDate as activityDate, sum(d.activeSeconds) as activeSeconds
            from UserActivityDailyTotal d
            join d.user u
            where d.activityDate between :startDate and :endDate
              and u.role.roleName = 'STUDENT'
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:studentId is null or u.userId = :studentId)
            group by d.activityDate
            order by d.activityDate
            """)
    List<DailyTotalProjection> sumForScope(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("studentId") Long studentId
    );

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO user_activity_daily_totals (user_id, activity_date, active_seconds)
            VALUES (:userId, :activityDate, :activeSeconds)
            ON CONFLICT (user_id, activity_date)
            DO UPDATE SET active_seconds =
                user_activity_daily_totals.active_seconds + EXCLUDED.active_seconds
            """, nativeQuery = true)
    int addActiveSeconds(
            @Param("userId") Long userId,
            @Param("activityDate") LocalDate activityDate,
            @Param("activeSeconds") long activeSeconds
    );
}
