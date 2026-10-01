package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.entity.UserActivitySession;
import com.project.ProjectS.model.ActivitySessionResponseDTO;
import com.project.ProjectS.repository.UserActivitySessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserActivitySessionService {
    public static final String ACTIVE = "ACTIVE";
    public static final String IDLE = "IDLE";
    public static final String CLOSED = "CLOSED";
    public static final String EXPIRED = "EXPIRED";

    private static final long MAX_HEARTBEAT_SECONDS = 45L;
    private static final Duration MISSED_HEARTBEAT = Duration.ofSeconds(90);
    private static final Duration AUTH_INACTIVITY_LIMIT = Duration.ofMinutes(30);

    private final UserActivitySessionRepository repository;

    public UserActivitySessionService(UserActivitySessionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ActivitySessionResponseDTO start(User user) {
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
        return toResponse(repository.save(session));
    }

    @Transactional
    public ActivitySessionResponseDTO heartbeat(User user, String sessionKey) {
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
        UserActivitySession session = getSession(user, sessionKey);
        closeSession(session, CLOSED, LocalDateTime.now());
        return toResponse(repository.save(session));
    }

    @Transactional
    public long totalTime(User user) {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByUser_UserIdAndStatusIn(
                        user.getUserId(), List.of(ACTIVE, IDLE, CLOSED, EXPIRED))
                .stream()
                .mapToLong(session -> session.getTotalActiveSeconds()
                        + pendingActiveSeconds(session, now))
                .sum();
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanStaleSessions() {
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
        session.setTotalActiveSeconds(session.getTotalActiveSeconds()
                + Math.min(elapsed, MAX_HEARTBEAT_SECONDS));
        session.setLastHeartbeatAt(now);
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
