package com.project.ProjectS.model;

import com.project.ProjectS.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Self-profile allowlist: credentials and Google subject IDs are never exposed. */
public record UserProfileDTO(Long userId, String name, String email, String role,
        Integer roleId, String phoneNumber, String address, String designation,
        Long employeeId, Long studentCode, String guardianName, String guardianPhoneNumber,
        Long collegeId, String collegeName, Long branchId, String branchName,
        Long courseId, String courseName, Long sectionId, String sectionName,
        String profilePicture, String loginType, Boolean activeRow,
        LocalDateTime lastLoginAt, BigDecimal weeklyStudyGoalHours,
        LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static UserProfileDTO from(User user) {
        return new UserProfileDTO(user.getUserId(), user.getName(), user.getEmail(),
                user.getRole() == null ? null : user.getRole().getRoleName(),
                user.getRole() == null ? null : user.getRole().getRoleId(),
                user.getPhoneNumber(), user.getAddress(), user.getDesignation(),
                user.getEmployeeId(), user.getStudentCode(), user.getGuardianName(), user.getGuardianPhoneNumber(),
                user.getCollege() == null ? null : user.getCollege().getCollegeId(),
                user.getCollege() == null ? null : user.getCollege().getInstituteName(),
                user.getBranch() == null ? null : user.getBranch().getBranchId(),
                user.getBranch() == null ? null : user.getBranch().getBranchName(),
                user.getCourse() == null ? null : user.getCourse().getCourseId(),
                user.getCourse() == null ? null : user.getCourse().getName(),
                user.getSection() == null ? null : user.getSection().getSectionId(),
                user.getSection() == null ? null : user.getSection().getSectionName(),
                user.getProfilePicture(), user.getLoginType(), user.getActiveRow(),
                user.getLastLoginAt(), user.getWeeklyStudyGoalHours(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
