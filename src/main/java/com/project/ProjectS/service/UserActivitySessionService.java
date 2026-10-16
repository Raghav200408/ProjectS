package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.entity.UserActivitySession;
import com.project.ProjectS.model.ActivitySessionResponseDTO;
import com.project.ProjectS.model.DailyActivityTimeResponseDTO;
import com.project.ProjectS.model.ActivitySessionRequestDTO;
import com.project.ProjectS.model.ContentDailyActivityTimeResponseDTO;
import com.project.ProjectS.model.StudentDailyEngagementDTO;
import com.project.ProjectS.model.StudentEngagementTrendsDTO;
import com.project.ProjectS.model.PeriodComparisonDTO;
import com.project.ProjectS.model.StudentConsistencyDTO;
import com.project.ProjectS.model.AtRiskStudentDTO;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.TopicRepository;
import com.project.ProjectS.repository.QuestionRepository;
import com.project.ProjectS.repository.UserActivityContentDailyTotalRepository;
import com.project.ProjectS.repository.UserActivityDailyTotalRepository;
import com.project.ProjectS.repository.UserActivitySessionRepository;
import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Topic;
import com.project.ProjectS.entity.Question;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashSet;
import java.util.Set;
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
    private final UserActivityContentDailyTotalRepository contentDailyRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public UserActivitySessionService(
            UserActivitySessionRepository repository,
            UserActivityDailyTotalRepository dailyTotalRepository,
            UserActivityContentDailyTotalRepository contentDailyRepository,
            ChapterRepository chapterRepository,
            TopicRepository topicRepository,
            QuestionRepository questionRepository) {
        this.repository = repository;
        this.dailyTotalRepository = dailyTotalRepository;
        this.contentDailyRepository = contentDailyRepository;
        this.chapterRepository = chapterRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    public UserActivitySessionService(
            UserActivitySessionRepository repository,
            UserActivityDailyTotalRepository dailyTotalRepository) {
        this(repository, dailyTotalRepository, null, null, null, null);
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
        return heartbeat(user, sessionKey, null);
    }

    @Transactional
    public ActivitySessionResponseDTO heartbeat(User user, ActivitySessionRequestDTO request) {
        return heartbeat(user, request.sessionKey(), request);
    }

    private ActivitySessionResponseDTO heartbeat(
            User user,
            String sessionKey,
            ActivitySessionRequestDTO contentContext) {
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
        applyContentContext(session, contentContext);
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
    public long totalTime(User user, Long requestedCollegeId, Long requestedBranchId, Long sectionId) {
        return totalTime(user, requestedCollegeId, requestedBranchId, sectionId, null);
    }

    @Transactional(readOnly = true)
    public long totalTime(User user, Long requestedCollegeId, Long requestedBranchId, Long sectionId, Long studentId) {
        String roleName = user.getRole() == null ? "" : user.getRole().getRoleName();
        if ("STUDENT".equalsIgnoreCase(roleName)) {
            if (requestedCollegeId != null || requestedBranchId != null || sectionId != null || studentId != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return totalTime(user);
        }
        ActivityScope scope = resolveAdminScope(user, requestedCollegeId, requestedBranchId, studentId, sectionId);
        LocalDateTime now = LocalDateTime.now();
        return repository.findStudentSessionsForScope(
                        scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId())
                .stream()
                .mapToLong(session -> session.getTotalActiveSeconds() + pendingActiveSeconds(session, now))
                .sum();
    }

    @Transactional(readOnly = true)
    public List<DailyActivityTimeResponseDTO> dailyTime(User user, int days) {
        return dailyTime(user, days, null, null, null);
    }

    @Transactional(readOnly = true)
    public PeriodComparisonDTO periodComparison(
            User user, int days, Long requestedCollegeId,
            Long requestedBranchId, Long sectionId) {
        return periodComparison(user, days, requestedCollegeId, requestedBranchId, sectionId, null);
    }

    @Transactional(readOnly = true)
    public PeriodComparisonDTO periodComparison(
            User user, int days, Long requestedCollegeId,
            Long requestedBranchId, Long sectionId, Long studentId) {
        if (days < 1 || days > MAX_DAILY_TIME_RANGE_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Days must be between 1 and 90");
        }
        LocalDate today = LocalDate.now();
        LocalDate currentStart = today.minusDays(days - 1L);
        LocalDate previousStart = currentStart.minusDays(days);
        LocalDate previousEnd = currentStart.minusDays(1);
        String roleName = user.getRole() == null ? "" : user.getRole().getRoleName();
        PeriodAccumulator current = new PeriodAccumulator();
        PeriodAccumulator previous = new PeriodAccumulator();
        Set<Long> currentStudents = new HashSet<>();
        Set<Long> previousStudents = new HashSet<>();
        long studentCount;

        if ("STUDENT".equalsIgnoreCase(roleName)) {
            if (requestedCollegeId != null || requestedBranchId != null || sectionId != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            dailyTotalRepository.findByUser_UserIdAndActivityDateBetweenOrderByActivityDateAsc(
                            user.getUserId(), previousStart, today)
                    .forEach(row -> addPeriodRow(row.getActivityDate(), row.getActiveSeconds(), user.getUserId(),
                            currentStart, current, previous, currentStudents, previousStudents));
            studentCount = 1;
            LocalDateTime now = LocalDateTime.now();
            repository.findByUser_UserIdAndStatusIn(user.getUserId(), List.of(ACTIVE))
                    .forEach(session -> splitByDate(session.getLastHeartbeatAt(), pendingActiveSeconds(session, now))
                            .forEach((date, seconds) -> {
                                if (!date.isBefore(currentStart) && !date.isAfter(today)) current.seconds += seconds;
                            }));
        } else {
            ActivityScope scope = resolveAdminScope(user, requestedCollegeId, requestedBranchId, studentId, sectionId);
            dailyTotalRepository.findStudentActivityRowsForScope(
                            previousStart, today, scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId())
                    .forEach(row -> addPeriodRow(row.getActivityDate(), row.getActiveSeconds(), row.getUserId(),
                            currentStart, current, previous, currentStudents, previousStudents));
            LocalDateTime now = LocalDateTime.now();
            repository.findActiveStudentSessionsForScope(
                            scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId())
                    .forEach(session -> {
                        Long id = session.getUser().getUserId();
                        splitByDate(session.getLastHeartbeatAt(), pendingActiveSeconds(session, now))
                                .forEach((date, seconds) -> {
                                    if (!date.isBefore(currentStart) && !date.isAfter(today) && seconds > 0) {
                                        current.seconds += seconds;
                                        currentStudents.add(id);
                                    }
                                });
                    });
            studentCount = dailyTotalRepository.countStudentsForScope(
                    scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId());
        }
        return new PeriodComparisonDTO(days, current.seconds, previous.seconds,
                current.activeDays, previous.activeDays, currentStudents.size(),
                previousStudents.size(), studentCount);
    }

    private static void addPeriodRow(
            LocalDate date, long seconds, Long studentId, LocalDate currentStart,
            PeriodAccumulator current, PeriodAccumulator previous,
            Set<Long> currentStudents, Set<Long> previousStudents) {
        if (!date.isBefore(currentStart)) {
            current.seconds += seconds;
            current.activeDays++;
            currentStudents.add(studentId);
        } else {
            previous.seconds += seconds;
            previous.activeDays++;
            previousStudents.add(studentId);
        }
    }

    private static class PeriodAccumulator {
        private long seconds;
        private long activeDays;
    }

    @Transactional(readOnly = true)
    public StudentEngagementTrendsDTO studentEngagementTrends(
            User user,
            int days,
            Long requestedCollegeId,
            Long requestedBranchId,
            Long sectionId) {
        return studentEngagementTrends(user, days, requestedCollegeId, requestedBranchId, sectionId, null);
    }

    @Transactional(readOnly = true)
    public StudentEngagementTrendsDTO studentEngagementTrends(
            User user, int days, Long requestedCollegeId, Long requestedBranchId, Long sectionId, Long studentId) {
        if (days < 1 || days > MAX_DAILY_TIME_RANGE_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Days must be between 1 and " + MAX_DAILY_TIME_RANGE_DAYS
            );
        }
        ActivityScope scope = resolveAdminScope(
                user, requestedCollegeId, requestedBranchId, studentId, sectionId);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);
        Map<LocalDate, DailyEngagementAccumulator> daily = new LinkedHashMap<>();
        for (int offset = 0; offset < days; offset++) {
            daily.put(startDate.plusDays(offset), new DailyEngagementAccumulator());
        }

        Set<Long> activeStudentIds = new HashSet<>();
        dailyTotalRepository.findStudentActivityRowsForScope(
                        startDate, today, scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId())
                .forEach(row -> {
                    DailyEngagementAccumulator aggregate = daily.get(row.getActivityDate());
                    if (aggregate != null) {
                        aggregate.activeSeconds += row.getActiveSeconds();
                        aggregate.studentIds.add(row.getUserId());
                        activeStudentIds.add(row.getUserId());
                    }
                });

        LocalDateTime now = LocalDateTime.now();
        repository.findActiveStudentSessionsForScope(
                        scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId())
                .forEach(session -> {
                    long pending = pendingActiveSeconds(session, now);
                    if (pending <= 0) return;
                    Long activeStudentId = session.getUser().getUserId();
                    splitByDate(session.getLastHeartbeatAt(), pending).forEach((date, seconds) -> {
                        DailyEngagementAccumulator aggregate = daily.get(date);
                        if (aggregate != null) {
                            aggregate.activeSeconds += seconds;
                            aggregate.studentIds.add(activeStudentId);
                            activeStudentIds.add(activeStudentId);
                        }
                    });
                });

        List<StudentDailyEngagementDTO> dailyItems = daily.entrySet().stream()
                .map(entry -> new StudentDailyEngagementDTO(
                        entry.getKey(), entry.getValue().activeSeconds, entry.getValue().studentIds.size()))
                .toList();
        long totalStudents = dailyTotalRepository.countStudentsForScope(
                scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId());
        LocalDate consistencyStart = today.minusDays(29);
        Map<Long, StudentConsistencyAccumulator> studentConsistency = new LinkedHashMap<>();
        dailyTotalRepository.findStudentActivityRowsForScope(
                        consistencyStart, today, scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId())
                .forEach(row -> {
                    StudentConsistencyAccumulator summary = studentConsistency.computeIfAbsent(
                            row.getUserId(), ignored -> new StudentConsistencyAccumulator(row.getName()));
                    summary.activeDays++;
                    if (summary.lastActiveDate == null || row.getActivityDate().isAfter(summary.lastActiveDate)) {
                        summary.lastActiveDate = row.getActivityDate();
                    }
                });
        long lowConsistency = Math.max(0, totalStudents - studentConsistency.size());
        long regularConsistency = 0;
        long highConsistency = 0;
        LocalDate riskCutoff = today.minusDays(6);
        List<AtRiskStudentDTO> atRiskStudents = new java.util.ArrayList<>();
        for (StudentConsistencyAccumulator summary : studentConsistency.values()) {
            if (summary.activeDays <= 3) lowConsistency++;
            else if (summary.activeDays <= 10) regularConsistency++;
            else highConsistency++;
            if (summary.lastActiveDate.isBefore(riskCutoff)) {
                atRiskStudents.add(new AtRiskStudentDTO(
                        summary.name, summary.lastActiveDate, summary.activeDays));
            }
        }
        long atRiskCount = atRiskStudents.size();
        atRiskStudents.sort((first, second) -> second.lastActiveDate().compareTo(first.lastActiveDate()));
        if (atRiskStudents.size() > 100) atRiskStudents = new java.util.ArrayList<>(atRiskStudents.subList(0, 100));
        StudentConsistencyDTO consistency = new StudentConsistencyDTO(
                lowConsistency, regularConsistency, highConsistency,
                atRiskCount, atRiskStudents);
        return new StudentEngagementTrendsDTO(totalStudents, activeStudentIds.size(), dailyItems, consistency);
    }

    private static class StudentConsistencyAccumulator {
        private final String name;
        private long activeDays;
        private LocalDate lastActiveDate;
        private StudentConsistencyAccumulator(String name) { this.name = name; }
    }

    private static class DailyEngagementAccumulator {
        private long activeSeconds;
        private final Set<Long> studentIds = new HashSet<>();
    }

    @Transactional(readOnly = true)
    public List<DailyActivityTimeResponseDTO> dailyTime(
            User user,
            int days,
            Long requestedCollegeId,
            Long requestedBranchId,
            Long studentId) {
        return dailyTime(user, days, requestedCollegeId, requestedBranchId, studentId, null);
    }

    @Transactional(readOnly = true)
    public List<DailyActivityTimeResponseDTO> dailyTime(
            User user,
            int days,
            Long requestedCollegeId,
            Long requestedBranchId,
            Long studentId,
            Long sectionId) {
        if (days < 1 || days > MAX_DAILY_TIME_RANGE_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Days must be between 1 and " + MAX_DAILY_TIME_RANGE_DAYS
            );
        }
        String roleName = user.getRole() == null ? "" : user.getRole().getRoleName();
        if ("STUDENT".equalsIgnoreCase(roleName)) {
            if (requestedCollegeId != null || requestedBranchId != null || studentId != null || sectionId != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return personalDailyTime(user, days);
        }
        ActivityScope scope = resolveAdminScope(
                user, requestedCollegeId, requestedBranchId, studentId, sectionId);
        return scopedDailyTime(days, scope.collegeId(), scope.branchId(), scope.studentId(), scope.sectionId());
    }

    private ActivityScope resolveAdminScope(
            User user,
            Long requestedCollegeId,
            Long requestedBranchId,
            Long studentId,
            Long sectionId) {
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
            return new ActivityScope(requestedCollegeId, requestedBranchId, studentId, sectionId);
        }
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role)) {
            Long assignedCollegeId = user.getCollege() == null ? null : user.getCollege().getCollegeId();
            if (assignedCollegeId == null
                    || (requestedCollegeId != null && !requestedCollegeId.equals(assignedCollegeId))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return new ActivityScope(assignedCollegeId, requestedBranchId, studentId, sectionId);
        }
        if ("BRANCH_ADMIN".equalsIgnoreCase(role)) {
            Long assignedBranchId = user.getBranch() == null ? null : user.getBranch().getBranchId();
            Long assignedCollegeId = user.getBranch() == null || user.getBranch().getCollege() == null
                    ? null : user.getBranch().getCollege().getCollegeId();
            if (assignedBranchId == null
                    || (requestedBranchId != null && !requestedBranchId.equals(assignedBranchId))
                    || (requestedCollegeId != null && !requestedCollegeId.equals(assignedCollegeId))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return new ActivityScope(assignedCollegeId, assignedBranchId, studentId, sectionId);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private record ActivityScope(Long collegeId, Long branchId, Long studentId, Long sectionId) {}

    @Transactional(readOnly = true)
    public List<ContentDailyActivityTimeResponseDTO> contentDailyTime(
            User user,
            int days,
            String requestedLevel) {
        return contentDailyTime(user, days, requestedLevel, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<ContentDailyActivityTimeResponseDTO> contentDailyTime(
            User user,
            int days,
            String requestedLevel,
            Long requestedCollegeId,
            Long requestedBranchId,
            Long sectionId) {
        return contentDailyTime(user, days, requestedLevel, requestedCollegeId, requestedBranchId, sectionId, null);
    }

    @Transactional(readOnly = true)
    public List<ContentDailyActivityTimeResponseDTO> contentDailyTime(
            User user, int days, String requestedLevel, Long requestedCollegeId,
            Long requestedBranchId, Long sectionId, Long studentId) {
        if (days < 1 || days > MAX_DAILY_TIME_RANGE_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Days must be between 1 and " + MAX_DAILY_TIME_RANGE_DAYS
            );
        }
        String level = requestedLevel == null ? "" : requestedLevel.toUpperCase(Locale.ROOT);
        if (!List.of("CHAPTER", "TOPIC", "QUESTION").contains(level)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content level");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);
        String roleName = user.getRole() == null ? "" : user.getRole().getRoleName();
        if ("STUDENT".equalsIgnoreCase(roleName)) {
            if (requestedCollegeId != null || requestedBranchId != null || sectionId != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            return contentDailyRepository
                    .findByUser_UserIdAndContentLevelAndActivityDateBetweenOrderByActivityDateAscContentNameAsc(
                            user.getUserId(), level, startDate, today)
                    .stream()
                    .map(item -> new ContentDailyActivityTimeResponseDTO(
                            item.getActivityDate(), item.getContentLevel(), item.getContentId(),
                            item.getContentName(), item.getActiveSeconds()))
                    .toList();
        }

        ActivityScope scope = resolveAdminScope(
                user, requestedCollegeId, requestedBranchId, studentId, sectionId);
        return contentDailyRepository.sumContentForScope(
                        startDate, today, level, scope.collegeId(), scope.branchId(), scope.sectionId(), scope.studentId())
                .stream()
                .map(item -> new ContentDailyActivityTimeResponseDTO(
                        item.getActivityDate(), item.getContentLevel(), item.getContentId(),
                        item.getContentName(), item.getActiveSeconds()))
                .toList();
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
            Long studentId,
            Long sectionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate startDate = today.minusDays(days - 1L);
        Map<LocalDate, Long> totals = new LinkedHashMap<>();
        for (int day = 0; day < days; day++) {
            totals.put(startDate.plusDays(day), 0L);
        }

        dailyTotalRepository.sumForScope(startDate, today, collegeId, branchId, studentId, sectionId)
                .forEach(daily -> totals.merge(
                        daily.getActivityDate(), daily.getActiveSeconds(), Long::sum));

        repository.findActiveStudentSessionsForScope(collegeId, branchId, sectionId, studentId)
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
        if (activeSeconds > 0 && contentDailyRepository != null) {
            splitByDate(session.getLastHeartbeatAt(), activeSeconds)
                    .forEach((date, seconds) -> addContentTotals(session, date, seconds));
        }
        session.setLastHeartbeatAt(now);
    }

    private void addContentTotals(UserActivitySession session, LocalDate date, long seconds) {
        Long userId = session.getUser().getUserId();
        if (session.getCurrentChapterId() != null) {
            contentDailyRepository.addActiveSeconds(userId, date, "CHAPTER",
                    session.getCurrentChapterId(), session.getCurrentChapterName(), seconds);
        }
        if (session.getCurrentTopicId() != null) {
            contentDailyRepository.addActiveSeconds(userId, date, "TOPIC",
                    session.getCurrentTopicId(), session.getCurrentTopicName(), seconds);
        }
        if (session.getCurrentQuestionId() != null) {
            contentDailyRepository.addActiveSeconds(userId, date, "QUESTION",
                    session.getCurrentQuestionId(), session.getCurrentQuestionName(), seconds);
        }
    }

    private void applyContentContext(
            UserActivitySession session,
            ActivitySessionRequestDTO context) {
        if (context == null) return;
        clearContentContext(session);

        if (context.questionId() != null) {
            Question question = questionRepository.findById(context.questionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question not found"));
            Long chapterId = question.getChapter().getChapterId();
            Long topicId = question.getTopic().getTopicId();
            if ((context.chapterId() != null && !context.chapterId().equals(chapterId))
                    || (context.topicId() != null && !context.topicId().equals(topicId))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question content context does not match");
            }
            session.setCurrentChapterId(chapterId);
            session.setCurrentChapterName(question.getChapter().getName());
            session.setCurrentTopicId(topicId);
            session.setCurrentTopicName(question.getTopic().getName());
            session.setCurrentQuestionId(question.getQuestionId());
            session.setCurrentQuestionName(shortName(question.getQuestionText()));
            return;
        }

        if (context.topicId() != null) {
            Topic topic = topicRepository.findById(context.topicId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic not found"));
            Long chapterId = topic.getChapter().getChapterId();
            if (context.chapterId() != null && !context.chapterId().equals(chapterId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic does not belong to chapter");
            }
            session.setCurrentChapterId(chapterId);
            session.setCurrentChapterName(topic.getChapter().getName());
            session.setCurrentTopicId(topic.getTopicId());
            session.setCurrentTopicName(topic.getName());
            return;
        }

        if (context.chapterId() != null) {
            Chapter chapter = chapterRepository.findById(context.chapterId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chapter not found"));
            session.setCurrentChapterId(chapter.getChapterId());
            session.setCurrentChapterName(chapter.getName());
        }
    }

    private void clearContentContext(UserActivitySession session) {
        session.setCurrentChapterId(null);
        session.setCurrentChapterName(null);
        session.setCurrentTopicId(null);
        session.setCurrentTopicName(null);
        session.setCurrentQuestionId(null);
        session.setCurrentQuestionName(null);
    }

    private String shortName(String value) {
        if (value == null || value.isBlank()) return "Question";
        return value.length() <= 500 ? value : value.substring(0, 497) + "...";
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
