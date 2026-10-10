package com.project.ProjectS.service;

import com.project.ProjectS.repository.UserActivityDailyTotalRepository;
import com.project.ProjectS.repository.UserActivitySessionRepository;
import com.project.ProjectS.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserActivitySessionServiceTest {
    private final UserActivitySessionRepository sessions = mock(UserActivitySessionRepository.class);
    private final UserActivityDailyTotalRepository dailyTotals = mock(UserActivityDailyTotalRepository.class);
    private final UserActivitySessionService service =
            new UserActivitySessionService(sessions, dailyTotals);

    @Test
    void splitsActiveTimeAcrossMidnight() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 9, 23, 59, 50);

        assertEquals(
                Map.of(
                        LocalDate.of(2026, 10, 9), 10L,
                        LocalDate.of(2026, 10, 10), 20L
                ),
                UserActivitySessionService.splitByDate(start, 30)
        );
    }

    @Test
    void splitsSubsecondMidnightBoundaryWithoutLosingTime() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 9, 23, 59, 59, 800_000_000);

        assertEquals(
                Map.of(
                        LocalDate.of(2026, 10, 9), 1L,
                        LocalDate.of(2026, 10, 10), 1L
                ),
                UserActivitySessionService.splitByDate(start, 2)
        );
    }

    @Test
    void returnsEveryRequestedDateIncludingDaysWithoutActivity() {
        var user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getUserId()).thenReturn(5L);
        when(user.getRole().getRoleName()).thenReturn("STUDENT");
        when(dailyTotals.findByUser_UserIdAndActivityDateBetweenOrderByActivityDateAsc(
                eq(5L), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(sessions.findByUser_UserIdAndStatusIn(5L, List.of(UserActivitySessionService.ACTIVE)))
                .thenReturn(List.of());

        var result = service.dailyTime(user, 7);

        assertEquals(7, result.size());
        assertEquals(LocalDate.now().minusDays(6), result.get(0).date());
        assertEquals(LocalDate.now(), result.get(6).date());
        assertTrue(result.stream().allMatch(day -> day.activeSeconds() == 0));
    }

    @Test
    void rejectsDailyRangesOutsideTheSupportedLimit() {
        var user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getRole().getRoleName()).thenReturn("STUDENT");

        assertEquals(
                400,
                assertThrows(ResponseStatusException.class, () -> service.dailyTime(user, 91))
                        .getStatusCode().value()
        );
        verifyNoInteractions(dailyTotals, sessions);
    }

    @Test
    void superAdminCanQuerySelectedOrganizationScope() {
        User user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getRole().getRoleName()).thenReturn("SUPER_ADMIN");
        when(dailyTotals.sumForScope(any(LocalDate.class), any(LocalDate.class), eq(3L), eq(8L), eq(12L)))
                .thenReturn(List.of());
        when(sessions.findActiveStudentSessionsForScope(3L, 8L, 12L)).thenReturn(List.of());

        var result = service.dailyTime(user, 7, 3L, 8L, 12L);

        assertEquals(7, result.size());
        verify(dailyTotals).sumForScope(any(LocalDate.class), any(LocalDate.class), eq(3L), eq(8L), eq(12L));
        verify(sessions).findActiveStudentSessionsForScope(3L, 8L, 12L);
    }

    @Test
    void collegeAdminCannotQueryAnotherCollege() {
        User user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getRole().getRoleName()).thenReturn("COLLEGE_ADMIN");
        when(user.getCollege().getCollegeId()).thenReturn(3L);

        assertEquals(
                403,
                assertThrows(ResponseStatusException.class,
                        () -> service.dailyTime(user, 7, 4L, null, null))
                        .getStatusCode().value()
        );
        verifyNoInteractions(dailyTotals, sessions);
    }

    @Test
    void branchAdminCannotQueryAnotherBranchOrCollege() {
        User user = mock(User.class, RETURNS_DEEP_STUBS);
        when(user.getRole().getRoleName()).thenReturn("BRANCH_ADMIN");
        when(user.getBranch().getBranchId()).thenReturn(8L);
        when(user.getBranch().getCollege().getCollegeId()).thenReturn(3L);

        assertEquals(
                403,
                assertThrows(ResponseStatusException.class,
                        () -> service.dailyTime(user, 7, 3L, 9L, null))
                        .getStatusCode().value()
        );
        assertEquals(
                403,
                assertThrows(ResponseStatusException.class,
                        () -> service.dailyTime(user, 7, 4L, 8L, null))
                        .getStatusCode().value()
        );
        verifyNoInteractions(dailyTotals, sessions);
    }
}
