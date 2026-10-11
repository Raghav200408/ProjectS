package com.project.ProjectS.service;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.StudentLastLoginDTO;
import com.project.ProjectS.model.StudentLoginInactivityDTO;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class StudentLoginAnalyticsService {
    private static final int MAX_RESULTS = 100;
    private final UserRepository userRepository;

    public StudentLoginAnalyticsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public StudentLoginInactivityDTO getInactiveStudents(
            User requester, int days, Long requestedCollegeId,
            Long requestedBranchId, Long sectionId) {
        if (days < 1 || days > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Days must be between 1 and 90");
        }
        String role = requester.getRole() == null ? "" : requester.getRole().getRoleName();
        Long collegeId = requestedCollegeId;
        Long branchId = requestedBranchId;
        if ("COLLEGE_ADMIN".equalsIgnoreCase(role)) {
            Long assignedCollegeId = requester.getCollege() == null ? null : requester.getCollege().getCollegeId();
            if (assignedCollegeId == null || (collegeId != null && !collegeId.equals(assignedCollegeId))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            collegeId = assignedCollegeId;
        } else if ("BRANCH_ADMIN".equalsIgnoreCase(role)) {
            Long assignedBranchId = requester.getBranch() == null ? null : requester.getBranch().getBranchId();
            Long assignedCollegeId = requester.getBranch() == null || requester.getBranch().getCollege() == null
                    ? null : requester.getBranch().getCollege().getCollegeId();
            if (assignedBranchId == null || (branchId != null && !branchId.equals(assignedBranchId))
                    || (collegeId != null && !collegeId.equals(assignedCollegeId))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            collegeId = assignedCollegeId;
            branchId = assignedBranchId;
        } else if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        var page = userRepository.findStudentsNotLoggedInSince(
                cutoff, collegeId, branchId, sectionId,
                PageRequest.of(0, MAX_RESULTS, Sort.by(Sort.Direction.ASC, "lastLoginAt")));
        var students = page.getContent().stream()
                .map(student -> new StudentLastLoginDTO(
                        student.getUserId(), student.getName(), student.getLastLoginAt()))
                .toList();
        return new StudentLoginInactivityDTO(
                userRepository.countActiveStudentsInScope(collegeId, branchId, sectionId),
                userRepository.countStudentsNotLoggedInSince(cutoff, collegeId, branchId, sectionId),
                userRepository.countStudentsWithoutLoginRecord(collegeId, branchId, sectionId),
                students);
    }
}
