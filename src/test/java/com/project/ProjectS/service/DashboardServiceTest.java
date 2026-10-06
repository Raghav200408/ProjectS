package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.repository.DashboardRepository;
import com.project.ProjectS.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    private final LocalDate today = LocalDate.of(2026, 10, 5);

    @Test void streakCountsConsecutiveDaysAndIgnoresDuplicates() {
        assertEquals(3, DashboardService.calculateStreak(List.of(today, today, today.minusDays(1),
                today.minusDays(2), today.minusDays(4)), today));
    }
    @Test void yesterdayKeepsStreakUntilTodayEnds() {
        assertEquals(2, DashboardService.calculateStreak(List.of(today.minusDays(1), today.minusDays(2)), today));
        assertEquals(0, DashboardService.calculateStreak(List.of(today.minusDays(2)), today));
        assertEquals(0, DashboardService.calculateStreak(List.of(), today));
    }
    @Test void futureDatesDoNotExtendStreak() {
        assertEquals(1, DashboardService.calculateStreak(List.of(today, today.plusDays(1)), today));
    }
    @Test void practiceWeekStartsMondayAndDoesNotMarkFutureDays() {
        LocalDate wednesday = LocalDate.of(2026, 10, 7);
        var week = DashboardService.buildPracticeWeek(List.of(wednesday.minusDays(2), wednesday,
                wednesday, wednesday.plusDays(1), wednesday.minusDays(7)), wednesday);
        assertEquals(7, week.size());
        assertEquals(LocalDate.of(2026, 10, 5), week.get(0).date());
        assertTrue(week.get(0).practiced());
        assertFalse(week.get(1).practiced());
        assertTrue(week.get(2).today());
        assertTrue(week.get(2).practiced());
        assertTrue(week.get(3).future());
        assertFalse(week.get(3).practiced());
    }
    @Test void sundayAndYearBoundaryUseTheCorrectWeek() {
        var sunday = DashboardService.buildPracticeWeek(List.of(), LocalDate.of(2027, 1, 3));
        assertEquals(LocalDate.of(2026, 12, 28), sunday.get(0).date());
        assertTrue(sunday.get(6).today());
        assertTrue(sunday.stream().noneMatch(day -> day.practiced() || day.future()));
    }
    @Test void studentScopeCannotExposeOtherBranches() {
        User user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getRole().getRoleName()).thenReturn("STUDENT");
        when(user.getCollege().getCollegeId()).thenReturn(2L);
        when(user.getBranch().getBranchId()).thenReturn(3L);
        var scope = DashboardService.scopeFor(user);
        assertEquals(2L, scope.collegeId());
        assertEquals(3L, scope.branchId());
        assertTrue(scope.subscribedOnly());
        when(user.getCollege()).thenReturn(null);
        when(user.getBranch()).thenReturn(null);
        assertTrue(DashboardService.scopeFor(user).selfOnly());
    }
    @Test void adminWithoutAssignmentIsRejectedInsteadOfGettingGlobalData() {
        User user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getRole().getRoleName()).thenReturn("BRANCH_ADMIN");
        when(user.getCollege()).thenReturn(null);
        when(user.getBranch()).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> DashboardService.scopeFor(user));
    }
    @Test void unauthenticatedDashboardDoesNotQueryData() {
        var repository = mock(DashboardRepository.class);
        var users = mock(UserRepository.class);
        var service = new DashboardService(repository, users, 20);
        Authentication authentication = mock(Authentication.class);
        assertThrows(ResponseStatusException.class, () -> service.getDashboard(authentication));
        verifyNoInteractions(repository, users);
    }
}
