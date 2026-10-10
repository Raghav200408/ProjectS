package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.entity.UserActivitySession;
import com.project.ProjectS.model.ActivitySessionResponseDTO;
import com.project.ProjectS.model.DailyActivityTimeResponseDTO;
import com.project.ProjectS.repository.UserActivityDailyTotalRepository;
import com.project.ProjectS.repository.UserActivitySessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;

@Service
public class UserActivitySessionService {
    private static final Logger log = LogManager.getLogger(UserActivitySessionService.class);
    public static final String ACTIVE = "ACTIVE";
    public static final String IDLE = "IDLE";
    public static final String CLOSED = "CLOSED";
    public static final String EXPIRED = "EXPIRED";

    private static final long MAX_HEARTBEAT_SECONDS = 45L;
    private static final Duration MISSED_HEARTBEAT = Duration.ofSeconds(90);
    private static final Duration AUTH_INACTIVITY_LIMIT = Duration.ofMinutes(30);
    private static final int MAX_DAILY_TIME_RANGE_DAYS = 90;

    private final UserActivitySessionRepository repository;
    private final UserActivityDailyTotalRepository dailyTotalRepository;

    public UserActivitySessionService(
            UserActivitySessionRepository repository,
            UserActivityDailyTotalRepository dailyTotalRepository) {
        this.repository = repository;
        this.dailyTotalRepository = dailyTotalRepository;
    }

    @Transactional
    public ActivitySessionResponseDTO start(User user) {
        log.info("Starting activity session for userId={}", user.getUserId());
        LocalDateTime now = LocalDateTime.now();
        repository.findByUser_UserIdAndStatusIn(user.getUserId(), List.of(ACTIVE, IDLE))
                .forEach(session -> closeSession(session, CLOSED, now));

        UserActivitySession session = new UserActivitySession();
        session.setSessionKey(UUID.randomUUID().toString());
        session.setUser(user);
        session.setStatus(ACTIVE);
        session.setStartedAt(now);
        session.setLastHeartbeatAt(now);
        session.setLastInteractionAt(now);
        session.setTotalActiveSeconds(0L);
        ActivitySessionResponseDTO response = toResponse(repository.save(session));
        log.debug("Activity session created: userId={} sessionKey={} status={}", user.getUserId(), session.getSessionKey(), session.getStatus());
        return response;
    }

    @Transactional
    public ActivitySessionResponseDTO heartbeat(User user, String sessionKey) {
        log.debug("Heartbeat received: userId={} sessionKey={}", user.getUserId(), sessionKey);
        UserActivitySession session = getSession(user, sessionKey);
        LocalDateTime now = LocalDateTime.now();
        rejectExpiredSession(session, now);

        if (ACTIVE.equals(session.getStatus())) {
            addActiveTime(session, now);
        } else if (IDLE.equals(session.getStatus())) {
            session.setStatus(ACTIVE);
            session.setLastHeartbeatAt(now);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Activity session is closed");
        }
        session.setLastInteractionAt(now);
        return toResponse(repository.save(session));
    }

    @Transactional
    public ActivitySessionResponseDTO idle(User user, String sessionKey) {
        log.debug("Activity session set to idle: userId={} sessionKey={}", user.getUserId(), sessionKey);
        UserActivitySession session = getSession(user, sessionKey);
        LocalDateTime now = LocalDateTime.now();
        rejectExpiredSession(session, now);
        if (ACTIVE.equals(session.getStatus())) {
            addActiveTime(session, now);
            session.setStatus(IDLE);
        }
        return toResponse(repository.save(session));
    }

    @Transactional
    public ActivitySessionResponseDTO resume(User user, String sessionKey) {
        log.debug("Resuming activity session: userId={} sessionKey={}", user.getUserId(), sessionKey);
        UserActivitySession session = getSession(user, sessionKey);
        LocalDateTime now = LocalDateTime.now();
        rejectExpiredSession(session, now);
        if (!ACTIVE.equals(session.getStatus()) && !IDLE.equals(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Activity session is closed");
        }
        session.setStatus(ACTIVE);
        session.setLastHeartbeatAt(now);
        session.setLastInteractionAt(now);
        return toResponse(repository.save(session));
    }

    @Transactional
    public ActivitySessionResponseDTO close(User user, String sessionKey) {
        log.info("Closing activity session: userId={} sessionKey={}", user.getUserId(), sessionKey);
        UserActivitySession session = getSession(user, sessionKey);
        closeSession(session, CLOSED, LocalDateTime.now());
        return toResponse(repository.save(session));
    }

    @Transactional
    public long totalTime(User user) {
        LocalDateTime now = LocalDateTime.now();
        long total = repository.findByUser_UserIdAndStatusIn(
                        user.getUserId(), List.of(ACTIVE, IDLE, CLOSED, EXPIRED))
                .stream()
                .mapToLong(session -> session.getTotalActiveSeconds()
                        + pendingActiveSeconds(session, now))
                .sum();
        log.debug("Computed total active time: userId={} totalSeconds={}", user.getUserId(), total);
        return total;
    }

    @Transactional(readOnly = true)
    public List<DailyActivityTimeResponseDTO> dailyTime(User user, int days) {
        return dailyTime(user, days, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<DailyActivityTimeResponseDTO> dailyTime(
            User user,
            int days,
            Long requestedCollegeId,
            Long requestedBranchId,
            Long studentId) {
        if (days < 1 || days > MAX_DAILY_TIME_RANGE_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Days must be between 1 and " + MAX_DAILY_TIME_RANGE_DAYS
            );
        }

        String roleName = user.getRole() == null ? null : user.getRole().getRoleName();
        String role = roleName == null ? "" : roleName.toUpperCase(Locale.ROOT);
        Long collegeId = requestedCollegeId;
        Long branchId = requestedBranchId;

        switch (role) {
            case "STUDENT" -> {
                if (collegeId != null || branchId != null || studentId != null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
                return personalDailyTime(user, days);
            }
            case "SUPER_ADMIN" -> {
                // Super admins may choose any organization scope.
            }
            case "COLLEGE_ADMIN" -> {
                Long assignedCollegeId = user.getCollege() == null
                        ? null
                        : user.getCollege().getCollegeId();
                if (assignedCollegeId == null
                        || (collegeId != null && !collegeId.equals(assignedCollegeId))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
                collegeId = assignedCollegeId;
            }
            case "BRANCH_ADMIN" -> {
                Long assignedBranchId = user.getBranch() == null
                        ? null
                        : user.getBranch().getBranchId();
                Long assignedCollegeId = user.getBranch() == null
                        || user.getBranch().getCollege() == null
                        ? null
                        : user.getBranch().getCollege().getCollegeId();
                if (assignedBranchId == null
                        || (branchId != null && !branchId.equals(assignedBranchId))
                        || (requestedCollegeId != null
                        && !requestedCollegeId.equals(assignedCollegeId))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
                branchId = assignedBranchId;
                collegeId = assignedCollegeId;
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return scopedDailyTime(days, collegeId, branchId, studentId);
    }

    private List<DailyActivityTimeResponseDTO> personalDailyTime(User user, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate startDate = today.minusDays(days - 1L);
        Map<LocalDate, Long> totals = new LinkedHashMap<>();
        for (int day = 0; day < days; day++) {
            totals.put(startDate.plusDays(day), 0L);
        }

        dailyTotalRepository
                .findByUser_UserIdAndActivityDateBetweenOrderByActivityDateAsc(
                        user.getUserId(), startDate, today)
                .forEach(daily -> totals.merge(
                        daily.getActivityDate(), daily.getActiveSeconds(), Long::sum));

        repository.findByUser_UserIdAndStatusIn(user.getUserId(), List.of(ACTIVE))
                .forEach(session -> splitByDate(
                        session.getLastHeartbeatAt(),
                        pendingActiveSeconds(session, now)
                ).forEach((date, seconds) -> totals.merge(date, seconds, Long::sum)));

        return totals.entrySet().stream()
                .map(entry -> new DailyActivityTimeResponseDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DailyActivityTimeResponseDTO> scopedDailyTime(
            int days,
            Long collegeId,
            Long branchId,
            Long studentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate startDate = today.minusDays(days - 1L);
        Map<LocalDate, Long> totals = new LinkedHashMap<>();
        for (int day = 0; day < days; day++) {
            totals.put(startDate.plusDays(day), 0L);
        }

        dailyTotalRepository.sumForScope(startDate, today, collegeId, branchId, studentId)
                .forEach(daily -> totals.merge(
                        daily.getActivityDate(), daily.getActiveSeconds(), Long::sum));

        repository.findActiveStudentSessionsForScope(collegeId, branchId, studentId)
                .forEach(session -> splitByDate(
                        session.getLastHeartbeatAt(),
                        pendingActiveSeconds(session, now)
                ).forEach((date, seconds) -> totals.merge(date, seconds, Long::sum)));

        return totals.entrySet().stream()
                .map(entry -> new DailyActivityTimeResponseDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanStaleSessions() {
        log.info("Running stale activity session cleanup");
        LocalDateTime now = LocalDateTime.now();
        repository.findByStatusAndLastHeartbeatAtBefore(ACTIVE, now.minus(MISSED_HEARTBEAT))
                .forEach(session -> {
                    addActiveTime(session, session.getLastHeartbeatAt().plusSeconds(MAX_HEARTBEAT_SECONDS));
                    session.setStatus(IDLE);
                });

        repository.findByStatusInAndLastInteractionAtBefore(
                        List.of(ACTIVE, IDLE), now.minus(AUTH_INACTIVITY_LIMIT))
                .forEach(session -> closeSession(session, EXPIRED, now));
    }

    private UserActivitySession getSession(User user, String sessionKey) {
        return repository.findBySessionKeyAndUser_UserId(sessionKey, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity session not found"));
    }

    private void rejectExpiredSession(UserActivitySession session, LocalDateTime now) {
        if (Duration.between(session.getLastInteractionAt(), now).compareTo(AUTH_INACTIVITY_LIMIT) >= 0) {
            closeSession(session, EXPIRED, now);
            repository.save(session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired due to inactivity");
        }
    }

    private void addActiveTime(UserActivitySession session, LocalDateTime now) {
        long elapsed = Math.max(0L, Duration.between(session.getLastHeartbeatAt(), now).getSeconds());
        long activeSeconds = Math.min(elapsed, MAX_HEARTBEAT_SECONDS);
        session.setTotalActiveSeconds(session.getTotalActiveSeconds() + activeSeconds);
        splitByDate(session.getLastHeartbeatAt(), activeSeconds)
                .forEach((date, seconds) -> dailyTotalRepository.addActiveSeconds(
                        session.getUser().getUserId(), date, seconds));
        session.setLastHeartbeatAt(now);
    }

    static Map<LocalDate, Long> splitByDate(LocalDateTime intervalStart, long activeSeconds) {
        Map<LocalDate, Long> totals = new LinkedHashMap<>();
        LocalDateTime cursor = intervalStart;
        long remaining = activeSeconds;
        while (remaining > 0) {
            LocalDateTime nextDay = cursor.toLocalDate().plusDays(1).atStartOfDay();
            Duration untilNextDay = Duration.between(cursor, nextDay);
            long secondsUntilNextDay =
                    untilNextDay.getSeconds() + (untilNextDay.getNano() > 0 ? 1 : 0);
            long secondsForDate = Math.min(remaining, secondsUntilNextDay);
            totals.merge(cursor.toLocalDate(), secondsForDate, Long::sum);
            cursor = cursor.plusSeconds(secondsForDate);
            remaining -= secondsForDate;
        }
        return totals;
    }

    private long pendingActiveSeconds(UserActivitySession session, LocalDateTime now) {
        if (!ACTIVE.equals(session.getStatus())) return 0L;
        return Math.min(
                Math.max(0L, Duration.between(session.getLastHeartbeatAt(), now).getSeconds()),
                MAX_HEARTBEAT_SECONDS
        );
    }

    private void closeSession(UserActivitySession session, String status, LocalDateTime now) {
        if (ACTIVE.equals(session.getStatus())) addActiveTime(session, now);
        session.setStatus(status);
        session.setEndedAt(now);
    }

    private ActivitySessionResponseDTO toResponse(UserActivitySession session) {
        return new ActivitySessionResponseDTO(
                session.getSessionKey(), session.getStatus(), session.getTotalActiveSeconds());
    }
}
