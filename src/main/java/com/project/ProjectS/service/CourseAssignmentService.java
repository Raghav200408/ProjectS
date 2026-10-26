package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.CourseAssignmentDTO;
import com.project.ProjectS.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CourseAssignmentService {
    private final CourseAssignmentRepository assignments;
    private final CourseRepository courses;
    private final BranchRepository branches;
    private final UserRepository users;
    public CourseAssignmentService(CourseAssignmentRepository assignments, CourseRepository courses,
            BranchRepository branches, UserRepository users) {
        this.assignments = assignments; this.courses = courses; this.branches = branches; this.users = users;
    }
    public User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new AccessDeniedException("Sign in is required");
        return users.findByEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("User not found"));
    }
    public static String role(User user) {
        return user.getRole() == null ? "" : user.getRole().getRoleName().toUpperCase(Locale.ROOT);
    }
    public static boolean isManager(User user) { return Set.of("SUPER_ADMIN", "COLLEGE_ADMIN", "BRANCH_ADMIN").contains(role(user)); }
    public static boolean isSuperAdmin(User user) { return "SUPER_ADMIN".equals(role(user)); }
    public boolean canSeeCollege(User user, Long collegeId) {
        return isSuperAdmin(user) || (user.getCollege() != null && user.getCollege().getCollegeId().equals(collegeId));
    }
    public boolean canSeeBranch(User user, Branch branch) { return inScope(user, branch); }
    public boolean eligible(User user, Course course) {
        if (Boolean.FALSE.equals(user.getActiveRow())) return false;
        if (isSuperAdmin(user)) return true;
        if (Boolean.FALSE.equals(course.getActiveRow()) || (user.getCollege() != null && Boolean.FALSE.equals(user.getCollege().getActiveRow()))) return false;
        if (user.getBranch() != null) {
            Branch branch = user.getBranch();
            if (user.getCollege() == null || branch.getCollege() == null ||
                    !user.getCollege().getCollegeId().equals(branch.getCollege().getCollegeId())) return false;
            return !Boolean.FALSE.equals(branch.getActiveRow()) && assignments.existsByCourse_CourseIdAndBranch_BranchIdAndActiveRowTrue(
                    course.getCourseId(), branch.getBranchId());
        }
        if (user.getCollege() != null) return assignments.existsByCourse_CourseIdAndBranch_College_CollegeIdAndActiveRowTrue(
                course.getCourseId(), user.getCollege().getCollegeId());
        // Independent learners retain direct personal subscriptions, not institution-wide access.
        return "GUEST".equals(role(user));
    }
    public void requireEligible(User user, Course course) {
        if (!eligible(user, course)) throw new AccessDeniedException("Course is not assigned to your branch");
    }
    public void requireOrganization(User actor, Long collegeId, Long branchId) {
        if (isSuperAdmin(actor)) return;
        if (actor.getCollege() == null || !actor.getCollege().getCollegeId().equals(collegeId))
            throw new AccessDeniedException("College is outside your scope");
        if ("COLLEGE_ADMIN".equals(role(actor))) return;
        if ("BRANCH_ADMIN".equals(role(actor)) && actor.getBranch() != null && actor.getBranch().getBranchId().equals(branchId)) return;
        throw new AccessDeniedException("Branch is outside your scope");
    }
    public void requireAssignment(Long collegeId, Long branchId, Long courseId) {
        Branch branch = branches.findById(branchId).orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        Course course = courses.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (branch.getCollege() == null || !branch.getCollege().getCollegeId().equals(collegeId) ||
                Boolean.FALSE.equals(branch.getActiveRow()) || Boolean.FALSE.equals(course.getActiveRow()) ||
                !assignments.existsByCourse_CourseIdAndBranch_BranchIdAndActiveRowTrue(courseId, branchId))
            throw new IllegalArgumentException("Course must be actively assigned to the selected branch");
    }
    @Transactional
    public CourseAssignmentDTO assign(Long courseId, Long collegeId, Long branchId, boolean active) {
        User actor = currentUser();
        requireOrganization(actor, collegeId, branchId);
        Branch branch = branches.findById(branchId).orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        if (branch.getCollege() == null || !Objects.equals(branch.getCollege().getCollegeId(), collegeId))
            throw new IllegalArgumentException("Branch does not belong to this college");
        Course course = courses.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return dto(saveAssignment(course, branch, active));
    }
    @Transactional
    public CourseAssignment saveAssignment(Course course, Branch branch, boolean active) {
        CourseAssignment a = assignments.findByCourse_CourseIdAndBranch_BranchId(course.getCourseId(), branch.getBranchId()).orElseGet(CourseAssignment::new);
        a.setCourse(course); a.setBranch(branch); a.setActiveRow(active);
        return assignments.save(a);
    }
    public List<CourseAssignmentDTO> list(Long courseId, User actor) {
        return assignments.findByCourse_CourseId(courseId).stream().filter(a -> inScope(actor, a.getBranch())).map(CourseAssignmentService::dto).toList();
    }
    private boolean inScope(User actor, Branch branch) {
        if (isSuperAdmin(actor)) return true;
        if (actor.getBranch() != null) return actor.getBranch().getBranchId().equals(branch.getBranchId());
        return actor.getCollege() != null && branch.getCollege() != null && actor.getCollege().getCollegeId().equals(branch.getCollege().getCollegeId());
    }
    private static CourseAssignmentDTO dto(CourseAssignment a) {
        return new CourseAssignmentDTO(a.getAssignmentId(), a.getCourse().getCourseId(), a.getBranch().getCollege().getCollegeId(),
                a.getBranch().getCollege().getInstituteName(), a.getBranch().getBranchId(), a.getBranch().getBranchName(), a.getActiveRow());
    }
}
