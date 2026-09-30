package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.PracticePerformanceItemDTO;
import com.project.ProjectS.model.PracticePerformanceResponseDTO;
import com.project.ProjectS.repository.PracticePerformanceRepository;
import com.project.ProjectS.repository.PracticePerformanceRepository.Filters;
import com.project.ProjectS.repository.PracticePerformanceRepository.Level;
import com.project.ProjectS.repository.PracticePerformanceRepository.Row;
import com.project.ProjectS.repository.PracticePerformanceRepository.Scope;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Practice performance at course / subject / chapter / topic level, worked out
 * from practice_results plus the question / unit definitions.
 *
 * Who sees what (same convention as the exam performance screens):
 *   STUDENT        -> only themselves
 *   BRANCH_ADMIN   -> students of their branch
 *   COLLEGE_ADMIN  -> students of their college (optionally one branch)
 *   SUPER_ADMIN    -> every student (optionally one college / branch)
 * Any role can further narrow to one student with studentId; that only ever
 * narrows, it can't reach outside the role's own scope.
 */
@Service
@Transactional(readOnly = true)
public class PracticePerformanceService {

    private final PracticePerformanceRepository performanceRepository;
    private final UserRepository userRepository;

    @Autowired
    public PracticePerformanceService(
            PracticePerformanceRepository performanceRepository,
            UserRepository userRepository) {

        this.performanceRepository = performanceRepository;
        this.userRepository = userRepository;
    }

    public PracticePerformanceResponseDTO getPerformance(
            Level level,
            Filters filters,
            Long collegeId,
            Long branchId,
            Long studentId,
            Authentication authentication) {

        Scope scope = resolveScope(
                getLoggedInUser(authentication),
                collegeId, branchId, studentId);

        List<Row> rows =
                performanceRepository.findMetrics(level, filters, scope);

        List<PracticePerformanceItemDTO> items =
                rows.stream().map(this::toItem).toList();

        // Headline numbers: add the counts up first, then take the
        // percentages - never an average of the items' percentages.
        long total = rows.stream().mapToLong(Row::totalUnits).sum();
        long attempted = rows.stream().mapToLong(Row::attemptedUnits).sum();
        long correct = rows.stream().mapToLong(Row::correctUnits).sum();

        PracticePerformanceItemDTO summary =
                toItem(new Row(0, "Total", null, total, attempted, correct));
        summary.setId(null);

        PracticePerformanceResponseDTO response =
                new PracticePerformanceResponseDTO();

        response.setLevel(level.name());
        response.setStudentCount(performanceRepository.countStudents(scope));
        response.setSummary(summary);
        response.setItems(items);

        return response;
    }

    private Scope resolveScope(
            User user,
            Long collegeId,
            Long branchId,
            Long studentId) {

        String role = user.getRole() == null
                ? ""
                : user.getRole().getRoleName();

        return switch (role) {

            case "STUDENT" ->
                    new Scope(null, null, user.getUserId());

            case "BRANCH_ADMIN" -> {
                if (user.getBranch() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Branch is not assigned to this user");
                }
                yield new Scope(
                        null, user.getBranch().getBranchId(), studentId);
            }

            case "COLLEGE_ADMIN" -> {
                if (user.getCollege() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "College is not assigned to this user");
                }
                yield new Scope(
                        user.getCollege().getCollegeId(), branchId, studentId);
            }

            case "SUPER_ADMIN" ->
                    new Scope(collegeId, branchId, studentId);

            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Practice performance is not available for this role");
        };
    }

    private PracticePerformanceItemDTO toItem(Row row) {

        PracticePerformanceItemDTO item = new PracticePerformanceItemDTO();

        item.setId(row.id());
        item.setName(row.name());
        item.setParentName(row.parentName());
        item.setTotalUnits(row.totalUnits());
        item.setAttemptedUnits(row.attemptedUnits());
        item.setCorrectUnits(row.correctUnits());
        item.setCompletionPercentage(
                percentage(row.attemptedUnits(), row.totalUnits()));
        item.setAccuracyPercentage(
                percentage(row.correctUnits(), row.attemptedUnits()));

        return item;
    }

    // part / whole * 100 to two decimals; 0.00 when the whole is 0.
    private BigDecimal percentage(long part, long whole) {

        if (whole == 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(whole), 2, RoundingMode.HALF_UP);
    }

    private User getLoggedInUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
