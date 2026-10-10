package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.DashboardResponseDTO;
import com.project.ProjectS.model.DashboardResponseDTO.PracticeDay;
import com.project.ProjectS.repository.DashboardRepository;
import com.project.ProjectS.repository.DashboardRepository.Scope;
import com.project.ProjectS.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class DashboardService {
    private static final Logger log = LogManager.getLogger(DashboardService.class);

    private final DashboardRepository repository;
    private final UserRepository users;
    private final int dailyGoal;

    public DashboardService(DashboardRepository repository, UserRepository users,
            @Value("${dashboard.practice.daily-goal:20}") int dailyGoal) {
        this.repository = repository;
        this.users = users;
        this.dailyGoal = Math.max(1, dailyGoal);
        log.info("DashboardService initialized with dailyGoal={}", this.dailyGoal);
    }

    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboard(Authentication authentication) {
        log.info("Dashboard request received for principal={}", authentication == null ? null : authentication.getName());
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Dashboard access denied: missing or unauthenticated principal");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        User user = users.findByEmail(authentication.getName())
                .orElseThrow(() -> {
                    log.warn("Dashboard access denied: user not found for email={}", authentication.getName());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                });
        Scope scope = scopeFor(user);
        log.info("Dashboard scope resolved for userId={} role={} scope={}", user.getUserId(), user.getRole() == null ? null : user.getRole().getRoleName(), scope);
        var now = OffsetDateTime.now(DashboardRepository.PRACTICE_ZONE);
        var result = new DashboardResponseDTO();
        repository.populateCounts(result, user.getUserId(), scope);
        double completed = result.getTotalUnits() == 0 ? 0
                : Math.round(10000.0 * result.getAttemptedUnits() / result.getTotalUnits()) / 100.0;
        result.setCompletionPercentage(completed);
        result.setPendingPercentage(result.getTotalUnits() == 0 ? 0 : Math.round((100 - completed) * 100) / 100.0);
        var ranking = repository.ranking(user.getUserId(), scope);
        result.setRank(ranking.currentRank());
        result.setRankedStudents(ranking.studentCount());
        result.setLeaderboard(ranking.entries());
        result.setRankingScope(scope.selfOnly() ? "Your results" : scope.branchId() != null ? "Your branch"
                : scope.collegeId() != null ? "Your college" : "All students");
        result.setTodayQuestions(repository.todayQuestions(user.getUserId(), now.toLocalDate()));
        var practiceDays = repository.practiceDays(user.getUserId(), now.toLocalDate());
        result.setStreakDays(calculateStreak(practiceDays, now.toLocalDate()));
        result.setPracticeWeek(buildPracticeWeek(practiceDays, now.toLocalDate()));
        result.setDailyGoal(dailyGoal);
        result.setRecentActivities(repository.recentActivities(user.getUserId()));
        result.setRefreshedAt(now);
        log.info("Dashboard built successfully for userId={} totalUnits={} attemptedUnits={} rank={} refreshedAt={}",
                user.getUserId(), result.getTotalUnits(), result.getAttemptedUnits(), result.getRank(), now);
        return result;
    }

    static Scope scopeFor(User user) {
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        boolean learner = "STUDENT".equals(role) || "GUEST".equals(role);
        if ("SUPER_ADMIN".equals(role)) return new Scope(null, null, false, false);
        Long college = user.getCollege() == null ? null : user.getCollege().getCollegeId();
        Long branch = user.getBranch() == null ? null : user.getBranch().getBranchId();
        if ("COLLEGE_ADMIN".equals(role) && college != null) return new Scope(college, null, false, false);
        if (("BRANCH_ADMIN".equals(role) || learner) && branch != null) return new Scope(college, branch, false, learner);
        if (learner && college != null) return new Scope(college, null, false, true);
        if (learner) return new Scope(null, null, true, true);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dashboard organization is not assigned");
    }

    static int calculateStreak(List<LocalDate> days, LocalDate today) {
        var dates = new HashSet<>(days);
        LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (dates.contains(cursor)) { streak++; cursor = cursor.minusDays(1); }
        return streak;
    }

    static List<PracticeDay> buildPracticeWeek(List<LocalDate> days, LocalDate today) {
        var practiced = new HashSet<>(days);
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        return IntStream.range(0, 7).mapToObj(index -> {
            LocalDate date = monday.plusDays(index);
            return new PracticeDay(date, !date.isAfter(today) && practiced.contains(date),
                    date.equals(today), date.isAfter(today));
        }).toList();
    }
}
