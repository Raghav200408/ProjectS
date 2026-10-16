package com.project.ProjectS.repository;

import com.project.ProjectS.entity.UserActivityContentDailyTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface UserActivityContentDailyTotalRepository
        extends JpaRepository<UserActivityContentDailyTotal, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO public.user_activity_content_daily_totals
                (user_id, activity_date, content_level, content_id, content_name, active_seconds)
            VALUES (:userId, :activityDate, :contentLevel, :contentId, :contentName, :activeSeconds)
            ON CONFLICT (user_id, activity_date, content_level, content_id)
            DO UPDATE SET
                content_name = EXCLUDED.content_name,
                active_seconds = user_activity_content_daily_totals.active_seconds + EXCLUDED.active_seconds
            """, nativeQuery = true)
    void addActiveSeconds(
            @Param("userId") Long userId,
            @Param("activityDate") LocalDate activityDate,
            @Param("contentLevel") String contentLevel,
            @Param("contentId") Long contentId,
            @Param("contentName") String contentName,
            @Param("activeSeconds") long activeSeconds
    );

    List<UserActivityContentDailyTotal>
    findByUser_UserIdAndContentLevelAndActivityDateBetweenOrderByActivityDateAscContentNameAsc(
            Long userId,
            String contentLevel,
            LocalDate startDate,
            LocalDate endDate
    );

    interface ScopedContentDailyProjection {
        LocalDate getActivityDate();
        String getContentLevel();
        Long getContentId();
        String getContentName();
        Long getActiveSeconds();
    }

    @Query("""
            select d.activityDate as activityDate,
                   d.contentLevel as contentLevel,
                   d.contentId as contentId,
                   d.contentName as contentName,
                   sum(d.activeSeconds) as activeSeconds
            from UserActivityContentDailyTotal d
            join d.user u
            where d.activityDate between :startDate and :endDate
              and d.contentLevel = :contentLevel
              and u.role.roleName = 'STUDENT'
              and (:collegeId is null or u.college.collegeId = :collegeId)
              and (:branchId is null or u.branch.branchId = :branchId)
              and (:sectionId is null or u.section.sectionId = :sectionId)
              and (:studentId is null or u.userId = :studentId)
            group by d.activityDate, d.contentLevel, d.contentId, d.contentName
            order by d.activityDate asc, d.contentName asc
            """)
    List<ScopedContentDailyProjection> sumContentForScope(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("contentLevel") String contentLevel,
            @Param("collegeId") Long collegeId,
            @Param("branchId") Long branchId,
            @Param("sectionId") Long sectionId,
            @Param("studentId") Long studentId
    );
}
