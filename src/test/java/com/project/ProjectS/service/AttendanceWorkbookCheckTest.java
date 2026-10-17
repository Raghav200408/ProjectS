package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.repository.*;
import com.project.ProjectS.security.service.CustomUserDetails;
import com.project.ProjectS.util.ExcelReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Explicit, read-only database check; the write repository is always mocked. */
@EnabledIfEnvironmentVariable(named = "ATTENDANCE_WORKBOOK_CHECK", matches = "true")
class AttendanceWorkbookCheckTest {
    @Test void workbookValuesPassActualExcelParserAndAttendanceMapper() throws Exception {
        Path path = Path.of(System.getProperty("attendance.workbook"));
        var file = new MockMultipartFile("file", path.getFileName().toString(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Files.readAllBytes(path));
        var rows = new ExcelReader().readExcel(file);
        assertEquals(5, rows.size());
        for (var row : rows) {
            assertFalse(row.containsKey("roll_no"));
            for (String field : List.of("student_id", "student_name", "college", "branch", "course", "date", "status")) {
                assertNotNull(row.get(field), field);
                assertFalse(row.get(field).isBlank(), field);
            }
            var attendance = new com.project.ProjectS.mapper.AttendanceExcelMapper().map(row);
            assertEquals("A", attendance.getSection());
            assertNotNull(attendance.getAttendanceDate());
            assertTrue(attendance.getOutTime().isAfter(attendance.getInTime()));
        }
    }

    @Test void workbookPassesRealParserAndUploadValidationAgainstCurrentProfiles() throws Exception {
        Path path = Path.of(System.getProperty("attendance.workbook"));
        var file = new MockMultipartFile("file", path.getFileName().toString(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Files.readAllBytes(path));
        var rows = new ExcelReader().readExcel(file);
        var codes = rows.stream().map(row -> Long.valueOf(row.get("student_id"))).distinct().toList();
        var profiles = new ArrayList<User>();
        try (var connection = DriverManager.getConnection(System.getenv("DB_URL"), System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"))) {
            connection.setReadOnly(true);
            connection.setAutoCommit(false);
            String sql = """
                    SELECT u.student_id, u.name, c.college_id, c.institute_name,
                        b.branch_id, b.branch_name, co.course_id, co.name AS course_name
                    FROM users u LEFT JOIN branch b ON b.branch_id=u.branch_id
                    LEFT JOIN college c ON c.college_id=COALESCE(u.college_id,b.college_id)
                    LEFT JOIN courses co ON co.course_id=u.course_id
                    WHERE u.student_id IN (
                    """ + String.join(",", Collections.nCopies(codes.size(), "?")) + ")";
            try (var statement = connection.prepareStatement(sql)) {
                for (int i=0; i<codes.size(); i++) statement.setLong(i+1, codes.get(i));
                try (var results = statement.executeQuery()) {
                    while (results.next()) {
                        var user = new User(); user.setStudentCode(results.getLong("student_id")); user.setName(results.getString("name"));
                        if (results.getObject("college_id") != null) {
                            var college = new College(); college.setCollegeId(results.getLong("college_id"));
                            college.setInstituteName(results.getString("institute_name")); user.setCollege(college);
                        }
                        if (results.getObject("branch_id") != null) {
                            var branch = new Branch(); branch.setBranchId(results.getLong("branch_id"));
                            branch.setBranchName(results.getString("branch_name")); user.setBranch(branch);
                        }
                        if (results.getObject("course_id") != null) {
                            var course = new Course(); course.setCourseId(results.getLong("course_id"));
                            course.setName(results.getString("course_name")); user.setCourse(course);
                        }
                        profiles.add(user);
                    }
                }
            }
            connection.rollback();
        }
        var records = mock(AttendanceRepository.class);
        var users = mock(UserRepository.class);
        when(users.findByStudentCodeIn(anyCollection())).thenReturn(profiles);
        var service = new AttendanceService(records, users);
        var role = new Role(); role.setRoleName("SUPER_ADMIN");
        var caller = new User(); caller.setRole(role);
        var auth = new UsernamePasswordAuthenticationToken(new CustomUserDetails(caller), null);
        var errors = new ArrayList<String>();
        for (var row : rows) {
            try { service.uploadAttendance(List.of(row), auth); }
            catch (IllegalArgumentException error) { errors.add(error.getMessage()); }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
        System.out.println("Workbook verified: " + rows.size() + " attendance rows; no database writes performed.");
    }
}
