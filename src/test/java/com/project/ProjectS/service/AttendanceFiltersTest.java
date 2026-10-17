package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.AttendanceExcelMapper;
import com.project.ProjectS.repository.*;
import com.project.ProjectS.security.service.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyCollection;

class AttendanceFiltersTest {
    @Test void listIncludesProfileFiltersAndRestrictsCollegeAdmins() {
        var records = mock(AttendanceRepository.class);
        var users = mock(UserRepository.class);
        var service = new AttendanceService(records, users);
        var college = new College(); college.setCollegeId(2L); college.setInstituteName("College Two");
        var branch = new Branch(); branch.setBranchId(4L); branch.setBranchName("Branch Four"); branch.setCollege(college);
        var course = new Course(); course.setCourseId(3L); course.setName("Commerce");
        var student = new User(); student.setStudentCode(30011L); student.setName("Nikhil");
        student.setCollege(college); student.setBranch(branch); student.setCourse(course);
        var row = new Attendance(); row.setStudentId("30011"); row.setStudentName("Nikhil");
        var unknown = new Attendance(); unknown.setStudentId("9999");
        when(records.findAll()).thenReturn(List.of(row, unknown));
        when(users.findByStudentCodeIn(anyCollection())).thenReturn(List.of(student));
        var role = new Role(); role.setRoleName("COLLEGE_ADMIN");
        var caller = new User(); caller.setRole(role); caller.setCollege(college);
        var auth = new UsernamePasswordAuthenticationToken(new CustomUserDetails(caller), null);
        var result = service.getAllAttendance(auth);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getCollegeId());
        assertEquals(4L, result.get(0).getBranchId());
        assertEquals(3L, result.get(0).getCourseId());
        var otherCollege = new College(); otherCollege.setCollegeId(9L); caller.setCollege(otherCollege);
        assertTrue(service.getAllAttendance(auth).isEmpty());
        role.setRoleName("SUPER_ADMIN");
        assertEquals(2, service.getAllAttendance(auth).size());
    }

    @Test void uploadUsesStudentIdWithoutRollNumber() {
        var row = new AttendanceExcelMapper().map(Map.of("student_id", "30011", "student_name", "Nikhil",
                "section", "A", "date", "2026-10-16", "college_id", "2", "branch_id", "4", "course_id", "3"));
        assertEquals("30011", row.getStudentId());
        assertEquals("A", row.getSection());
    }

    @Test void nameUploadMatchesAssignedHierarchyAndRejectsWrongNamesBeforeSaving() {
        var records = mock(AttendanceRepository.class);
        var users = mock(UserRepository.class);
        var service = new AttendanceService(records, users);
        var college = new College(); college.setInstituteName("Example College");
        var branch = new Branch(); branch.setBranchName("Commerce Branch"); branch.setCollege(college);
        var course = new Course(); course.setName("MEC");
        var student = new User(); student.setStudentCode(30011L); student.setName("Nikhil");
        student.setCollege(college); student.setBranch(branch); student.setCourse(course);
        when(users.findByStudentCodeIn(anyCollection())).thenReturn(List.of(student));
        var role = new Role(); role.setRoleName("SUPER_ADMIN");
        var caller = new User(); caller.setRole(role);
        var auth = new UsernamePasswordAuthenticationToken(new CustomUserDetails(caller), null);
        var row = new java.util.HashMap<String, String>(Map.of("student_id", "30011", "student_name", "Nikhil",
                "college", " example college ", "branch", "COMMERCE BRANCH", "course", "MEC",
                "date", "2026-10-16", "section", "A", "_excel_row_number", "2"));
        service.uploadAttendance(List.of(row), auth);
        verify(records).saveAll(anyList());
        clearInvocations(records);
        row.put("course", "Wrong course");
        var error = assertThrows(IllegalArgumentException.class, () -> service.uploadAttendance(List.of(row), auth));
        assertTrue(error.getMessage().contains("Row 2: course name"));
        verify(records, never()).saveAll(anyList());
    }
}
