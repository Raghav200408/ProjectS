package com.project.ProjectS.service;

import com.project.ProjectS.entity.Attendance;
import com.project.ProjectS.model.AttendanceRequestDTO;
import com.project.ProjectS.model.AttendanceResponseDTO;
import com.project.ProjectS.repository.AttendanceRepository;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.entity.User;
import org.springframework.transaction.annotation.Transactional;
import com.project.ProjectS.security.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
    private static final Logger log = LogManager.getLogger(AttendanceService.class);
    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    // Create Attendance
    public AttendanceResponseDTO createAttendance(
            AttendanceRequestDTO requestDTO) {
        log.info("Creating attendance: studentId={} date={} section={}", requestDTO.getStudentId(), requestDTO.getAttendanceDate(), requestDTO.getSection());

        Attendance attendance = new Attendance();

        attendance.setStudentId(requestDTO.getStudentId());
        attendance.setStudentName(requestDTO.getStudentName());
        attendance.setSection(requestDTO.getSection());
        attendance.setAttendanceDate(requestDTO.getAttendanceDate());
        attendance.setDay(requestDTO.getDay());
        attendance.setInTime(requestDTO.getInTime());
        attendance.setOutTime(requestDTO.getOutTime());
        attendance.setStatus(requestDTO.getStatus());
        attendance.setStatusDescription(
                requestDTO.getStatusDescription()
        );

        Attendance savedAttendance =
                attendanceRepository.save(attendance);
        log.info("Attendance created successfully: attendanceId={} studentId={}", savedAttendance.getAttendanceId(), savedAttendance.getStudentId());

        return convertToResponseDTO(savedAttendance);
    }

    // Get Attendance by ID
    public AttendanceResponseDTO getAttendanceById(
            Long attendanceId) {

        Attendance attendance =
                attendanceRepository.findById(attendanceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attendance not found with id: "
                                                + attendanceId
                                )
                        );

        return convertToResponseDTO(attendance);
    }

    // Get All Attendance
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAllAttendance(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Authenticated admin is required");
        }
        User caller = details.getUser();
        String role = caller.getRole().getRoleName();
        if (!java.util.Set.of("SUPER_ADMIN", "COLLEGE_ADMIN", "BRANCH_ADMIN").contains(role)) {
            throw new AccessDeniedException("Admin access is required");
        }
        var records = attendanceRepository.findAll();
        var codes = records.stream().map(row -> studentCode(row.getStudentId()))
                .filter(java.util.Objects::nonNull).distinct().toList();
        var students = codes.isEmpty() ? java.util.List.<User>of() : userRepository.findByStudentCodeIn(codes);
        var byCode = students.stream().collect(Collectors.groupingBy(User::getStudentCode));
        var result = new java.util.ArrayList<AttendanceResponseDTO>();
        for (Attendance row : records) {
            var code = studentCode(row.getStudentId());
            var matches = code == null ? java.util.List.<User>of() : byCode.getOrDefault(code, java.util.List.of());
            if (matches.size() > 1) {
                matches = matches.stream().filter(user -> user.getName() != null && row.getStudentName() != null &&
                        user.getName().trim().equalsIgnoreCase(row.getStudentName().trim())).toList();
            }
            User student = matches.size() == 1 ? matches.get(0) : null;
            var college = student == null ? null : student.getCollege() != null ? student.getCollege()
                    : student.getBranch() == null ? null : student.getBranch().getCollege();
            boolean permitted = role.equals("SUPER_ADMIN") || student != null && (
                    role.equals("COLLEGE_ADMIN") && caller.getCollege() != null && college != null &&
                            caller.getCollege().getCollegeId().equals(college.getCollegeId()) ||
                    role.equals("BRANCH_ADMIN") && caller.getBranch() != null && student.getBranch() != null &&
                            caller.getBranch().getBranchId().equals(student.getBranch().getBranchId()));
            if (!permitted) continue;
            var dto = convertToResponseDTO(row);
            if (college != null) { dto.setCollegeId(college.getCollegeId()); dto.setCollegeName(college.getInstituteName()); }
            if (student != null && student.getBranch() != null) {
                dto.setBranchId(student.getBranch().getBranchId()); dto.setBranchName(student.getBranch().getBranchName());
            }
            if (student != null && student.getCourse() != null) {
                dto.setCourseId(student.getCourse().getCourseId()); dto.setCourseName(student.getCourse().getName());
            }
            result.add(dto);
        }
        return result;
    }

    private Long studentCode(String value) {
        try { return value == null ? null : Long.valueOf(value.trim()); }
        catch (NumberFormatException exception) { return null; }
    }

    @Transactional
    public void uploadAttendance(List<java.util.Map<String, String>> rows, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Authenticated admin is required");
        }
        User caller = details.getUser();
        String role = caller.getRole().getRoleName();
        if (!java.util.Set.of("SUPER_ADMIN", "COLLEGE_ADMIN", "BRANCH_ADMIN").contains(role)) {
            throw new AccessDeniedException("Admin access is required");
        }
        var codes = rows.stream().map(row -> studentCode(row.get("student_id")))
                .filter(java.util.Objects::nonNull).distinct().toList();
        var students = codes.isEmpty() ? java.util.List.<User>of() : userRepository.findByStudentCodeIn(codes);
        var mapper = new com.project.ProjectS.mapper.AttendanceExcelMapper();
        var attendance = new java.util.ArrayList<Attendance>();
        for (var row : rows) {
            if (row.entrySet().stream().filter(entry -> !entry.getKey().startsWith("_"))
                    .allMatch(entry -> entry.getValue() == null || entry.getValue().isBlank())) continue;
            String prefix = "Row " + row.getOrDefault("_excel_row_number", "?") + ": ";
            var code = studentCode(row.get("student_id"));
            var matches = students.stream().filter(student -> java.util.Objects.equals(code, student.getStudentCode()) &&
                    sameName(student.getName(), row.get("student_name"))).toList();
            if (matches.size() != 1) throw new IllegalArgumentException(prefix + "Student ID and name must match exactly one existing student");
            var student = matches.get(0);
            var branch = student.getBranch();
            var college = student.getCollege() != null ? student.getCollege() : branch == null ? null : branch.getCollege();
            boolean permitted = role.equals("SUPER_ADMIN") ||
                    role.equals("COLLEGE_ADMIN") && caller.getCollege() != null && college != null &&
                            caller.getCollege().getCollegeId().equals(college.getCollegeId()) ||
                    role.equals("BRANCH_ADMIN") && caller.getBranch() != null && branch != null &&
                            caller.getBranch().getBranchId().equals(branch.getBranchId());
            if (!permitted) throw new AccessDeniedException(prefix + "Student is outside your assigned scope");
            validateUploadName(row, "college", college == null ? null : college.getInstituteName(), prefix);
            validateUploadName(row, "branch", branch == null ? null : branch.getBranchName(), prefix);
            validateUploadName(row, "course", student.getCourse() == null ? null : student.getCourse().getName(), prefix);
            try { attendance.add(mapper.map(row)); }
            catch (IllegalArgumentException exception) { throw new IllegalArgumentException(prefix + exception.getMessage(), exception); }
        }
        attendanceRepository.saveAll(attendance);
    }

    private boolean sameName(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private void validateUploadName(java.util.Map<String, String> row, String field, String actual, String prefix) {
        String supplied = row.getOrDefault(field, row.get(field + "_name"));
        if (supplied == null || supplied.isBlank()) {
            if (actual != null && !actual.isBlank()) throw new IllegalArgumentException(prefix + field + " name is required");
        } else if (!sameName(actual, supplied)) {
            throw new IllegalArgumentException(prefix + field + " name does not match the student's assigned " + field);
        }
    }

    // Update Attendance
    public AttendanceResponseDTO updateAttendance(
            Long attendanceId,
            AttendanceRequestDTO requestDTO) {

        Attendance attendance =
                attendanceRepository.findById(attendanceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attendance not found with id: "
                                                + attendanceId
                                )
                        );

        attendance.setStudentId(requestDTO.getStudentId());
        attendance.setStudentName(requestDTO.getStudentName());
        attendance.setSection(requestDTO.getSection());
        attendance.setAttendanceDate(requestDTO.getAttendanceDate());
        attendance.setDay(requestDTO.getDay());
        attendance.setInTime(requestDTO.getInTime());
        attendance.setOutTime(requestDTO.getOutTime());
        attendance.setStatus(requestDTO.getStatus());
        attendance.setStatusDescription(
                requestDTO.getStatusDescription()
        );

        Attendance updatedAttendance =
                attendanceRepository.save(attendance);

        return convertToResponseDTO(updatedAttendance);
    }

    // Delete Attendance
    public void deleteAttendance(Long attendanceId) {

        if (!attendanceRepository.existsById(attendanceId)) {
            throw new RuntimeException(
                    "Attendance not found with id: " + attendanceId
            );
        }

        attendanceRepository.deleteById(attendanceId);
    }

    /**
     * Returns attendance belonging only to the authenticated student.
     */
    public List<AttendanceResponseDTO> getOwnAttendance(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Authenticated student is required");
        }

        Long studentCode = details.getUser().getStudentCode();
        if (studentCode == null) {
            throw new AccessDeniedException("Student code is not assigned to this user");
        }

        return attendanceRepository.findByStudentId(studentCode.toString())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Convert Entity to Response DTO
    private AttendanceResponseDTO convertToResponseDTO(
            Attendance attendance) {

        AttendanceResponseDTO responseDTO =
                new AttendanceResponseDTO();

        responseDTO.setAttendanceId(
                attendance.getAttendanceId()
        );

        responseDTO.setStudentId(
                attendance.getStudentId()
        );

        responseDTO.setStudentName(
                attendance.getStudentName()
        );

        responseDTO.setSection(
                attendance.getSection()
        );

        responseDTO.setAttendanceDate(
                attendance.getAttendanceDate()
        );

        responseDTO.setDay(
                attendance.getDay()
        );

        responseDTO.setInTime(
                attendance.getInTime()
        );

        responseDTO.setOutTime(
                attendance.getOutTime()
        );

        responseDTO.setStatus(
                attendance.getStatus()
        );

        responseDTO.setStatusDescription(
                attendance.getStatusDescription()
        );

        responseDTO.setCreatedAt(
                attendance.getCreatedAt()
        );

        responseDTO.setUpdatedAt(
                attendance.getUpdatedAt()
        );

        return responseDTO;
    }
}
