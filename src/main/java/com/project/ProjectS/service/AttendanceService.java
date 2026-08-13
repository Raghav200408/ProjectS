package com.project.ProjectS.service;

import com.project.ProjectS.entity.Attendance;
import com.project.ProjectS.model.AttendanceRequestDTO;
import com.project.ProjectS.model.AttendanceResponseDTO;
import com.project.ProjectS.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Create Attendance
    public AttendanceResponseDTO createAttendance(
            AttendanceRequestDTO requestDTO) {

        Attendance attendance = new Attendance();

        attendance.setStudentId(requestDTO.getStudentId());
        attendance.setRollNo(requestDTO.getRollNo());
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
    public List<AttendanceResponseDTO> getAllAttendance() {

        return attendanceRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
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
        attendance.setRollNo(requestDTO.getRollNo());
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

        responseDTO.setRollNo(
                attendance.getRollNo()
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