package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    public UserResponseDTO createSuperAdmin(SuperAdminRequestDTO request) {

        validateUser(request.getEmail(), request.getPhoneNumber());

        Role role = getRole("SUPER_ADMIN");

        User user = new User();

        user.setName(request.getName());
        user.setDesignation(request.getDesignation());
        user.setAddress(request.getAddress());
        user.setEmployeeId(request.getEmployeeId());

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);

        user.setLoginType("NORMAL");

        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }




    public UserResponseDTO createBranchAdmin(
            BranchAdminRequestDTO request) {

        validateUser(request.getEmail(), request.getPhoneNumber());

        Role role = getRole("BRANCH_ADMIN");

        College college = getCollege(request.getCollegeId());

        Branch branch = getBranch(request.getBranchId());

        User user = new User();

        user.setName(request.getName());
        user.setDesignation(request.getDesignation());
        user.setAddress(request.getAddress());
        user.setEmployeeId(request.getEmployeeId());

        user.setCollege(college);
        user.setBranch(branch);

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);

        user.setLoginType("NORMAL");

        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }


    public UserResponseDTO createStudent(StudentRequestDTO request) {

        validateUser(request.getEmail(), request.getPhoneNumber());

        Role role = getRole("STUDENT");

        College college = getCollege(request.getCollegeId());

        Branch branch = getBranch(request.getBranchId());

        Section section = getSection(request.getSectionId());

        User user = new User();

        user.setName(request.getName());
        user.setStudentCode(request.getStudentCode());
        user.setAddress(request.getAddress());

        user.setCollege(college);
        user.setBranch(branch);
        user.setSection(section);

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        user.setGuardianName(request.getGuardianName());
        user.setGuardianPhoneNumber(
                request.getGuardianPhoneNumber()
        );

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);

        user.setLoginType("NORMAL");

        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }


    // =========================================================
    // GUEST REGISTRATION
    // =========================================================

    public UserResponseDTO registerGuest(
            GuestUserRequestDTO request) {

        validateUser(request.getEmail(), request.getPhoneNumber());

        Role role = getRole("GUEST");

        User user = new User();

        user.setName(request.getName());
        user.setAddress(request.getAddress());

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);

        user.setLoginType("NORMAL");

        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }

    public List<UserResponseDTO> getAllSuperAdmins() {

        List<User> users = userRepository.findByRole_RoleName("SUPER_ADMIN");

        return users.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<UserResponseDTO> getAllBranchAdmins(){
        List<User> users = userRepository.findByRole_RoleName("BRANCH_ADMIN");
        return users.stream()
                .map(this::convertToResponse)
                .toList();
    }
    public  List<UserResponseDTO> getAllStudents(){
        List<User> users = userRepository.findByRole_RoleName("STUDENT");
        return users.stream()
                .map(this::convertToResponse)
                .toList();
    }
    public UserResponseDTO getSuperAdminById(Long userId) {

        User user = userRepository
                .findByUserIdAndRole_RoleName(userId, "SUPER_ADMIN")
                .orElseThrow(() ->
                        new RuntimeException("Super Admin not found with id: " + userId));

        return convertToResponse(user);
    }
    public UserResponseDTO getBranchAdminById(Long userId) {

        User user = userRepository
                .findByUserIdAndRole_RoleName(userId, "BRANCH_ADMIN")
                .orElseThrow(() ->
                        new RuntimeException("Branch Admin not found with id: " + userId));

        return convertToResponse(user);
    }
    public UserResponseDTO getStudentById(Long userId) {

        User user = userRepository
                .findByUserIdAndRole_RoleName(userId, "STUDENT")
                .orElseThrow(() ->
                        new RuntimeException("Student not found with id: " + userId));

        return convertToResponse(user);
    }
    public UserResponseDTO getGuestById(Long userId) {

        User user = userRepository
                .findByUserIdAndRole_RoleName(userId, "GUEST")
                .orElseThrow(() ->
                        new RuntimeException("Guest not found with id: " + userId));

        return convertToResponse(user);
    }
    public UserResponseDTO updateSuperAdmin(
            Long userId,
            SuperAdminRequestDTO request) {

        User user = getUserByIdAndRole(userId, "SUPER_ADMIN");

        validateUserForUpdate(
                userId,
                request.getEmail(),
                request.getPhoneNumber()
        );

        user.setName(request.getName());
        user.setDesignation(request.getDesignation());
        user.setAddress(request.getAddress());
        user.setEmployeeId(request.getEmployeeId());

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        return convertToResponse(updatedUser);
    }
    public UserResponseDTO updateBranchAdmin(
            Long userId,
            BranchAdminRequestDTO request) {

        User user = getUserByIdAndRole(userId, "BRANCH_ADMIN");

        validateUserForUpdate(
                userId,
                request.getEmail(),
                request.getPhoneNumber()
        );

        user.setName(request.getName());
        user.setDesignation(request.getDesignation());
        user.setAddress(request.getAddress());
        user.setEmployeeId(request.getEmployeeId());

        user.setCollege(
                getCollege(request.getCollegeId())
        );

        user.setBranch(
                getBranch(request.getBranchId())
        );

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        return convertToResponse(updatedUser);
    }
    public UserResponseDTO updateStudent(
            Long userId,
            StudentRequestDTO request) {

        User user = getUserByIdAndRole(userId, "STUDENT");

        validateUserForUpdate(
                userId,
                request.getEmail(),
                request.getPhoneNumber()
        );

        user.setName(request.getName());
        user.setStudentCode(request.getStudentCode());
        user.setAddress(request.getAddress());

        user.setCollege(
                getCollege(request.getCollegeId())
        );

        user.setBranch(
                getBranch(request.getBranchId())
        );

        user.setSection(
                getSection(request.getSectionId())
        );

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        user.setGuardianName(request.getGuardianName());
        user.setGuardianPhoneNumber(
                request.getGuardianPhoneNumber()
        );

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        return convertToResponse(updatedUser);
    }
    public UserResponseDTO updateGuest(
            Long userId,
            GuestUserRequestDTO request) {

        User user = getUserByIdAndRole(userId, "GUEST");

        validateUserForUpdate(
                userId,
                request.getEmail(),
                request.getPhoneNumber()
        );

        user.setName(request.getName());
        user.setAddress(request.getAddress());

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        return convertToResponse(updatedUser);
    }
    public String deleteSuperAdmin(Long userId) {

        User user = getUserByIdAndRole(userId, "SUPER_ADMIN");

        user.setActiveRow(false);

        userRepository.save(user);

        return "Super Admin deleted successfully.";
    }
    public String deleteBranchAdmin(Long userId) {

        User user = getUserByIdAndRole(userId, "BRANCH_ADMIN");

        user.setActiveRow(false);

        userRepository.save(user);

        return "Branch Admin deleted successfully.";
    }
    public String deleteStudent(Long userId) {

        User user = getUserByIdAndRole(userId, "STUDENT");

        user.setActiveRow(false);

        userRepository.save(user);

        return "Student deleted successfully.";
    }
    public String deleteGuest(Long userId) {

        User user = getUserByIdAndRole(userId, "GUEST");

        user.setActiveRow(false);

        userRepository.save(user);

        return "Guest deleted successfully.";
    }


    // =========================================================
    // VALIDATE EMAIL AND PHONE
    // =========================================================

    private void validateUser(String email, String phoneNumber) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "User already exists with email: " + email
            );
        }

        if (phoneNumber != null &&
                userRepository.existsByPhoneNumber(phoneNumber)) {

            throw new RuntimeException(
                    "User already exists with phone number: "
                            + phoneNumber
            );
        }
    }


    private Role getRole(String roleName) {

        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found: " + roleName
                        )
                );
    }


    // =========================================================
    // GET COLLEGE
    // =========================================================

    private College getCollege(Long collegeId) {

        return collegeRepository.findById(collegeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "College not found with id: "
                                        + collegeId
                        )
                );
    }


     //get branch
    private Branch getBranch(Long branchId) {

        return branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Branch not found with id: "
                                        + branchId
                        )
                );
    }

    //get section
    private Section getSection(Long sectionId) {

        return sectionRepository.findById(sectionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Section not found with id: "
                                        + sectionId
                        )
                );
    }
    private void validateUserForUpdate(
            Long userId,
            String email,
            String phoneNumber) {

        if (userRepository.existsByEmailAndUserIdNot(email, userId)) {
            throw new RuntimeException(
                    "User already exists with email: " + email
            );
        }

        if (phoneNumber != null &&
                userRepository.existsByPhoneNumberAndUserIdNot(phoneNumber, userId)) {

            throw new RuntimeException(
                    "User already exists with phone number: " + phoneNumber
            );
        }
    }
    private User getUserByIdAndRole(Long userId, String roleName) {

        return userRepository
                .findByUserIdAndRole_RoleName(userId, roleName)
                .orElseThrow(() ->
                        new RuntimeException(
                                roleName + " not found with id: " + userId
                        ));
    }



     //Entity to Response
    private UserResponseDTO convertToResponse(User user) {

        UserResponseDTO dto = new UserResponseDTO();

        dto.setUserId(user.getUserId());

        dto.setName(user.getName());
        dto.setDesignation(user.getDesignation());
        dto.setAddress(user.getAddress());

        dto.setEmployeeId(user.getEmployeeId());
        dto.setStudentCode(user.getStudentCode());

        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());

        dto.setGuardianName(user.getGuardianName());
        dto.setGuardianPhoneNumber(
                user.getGuardianPhoneNumber()
        );

        // College
        if (user.getCollege() != null) {

            dto.setCollegeId(
                    user.getCollege().getCollegeId()
            );

            dto.setCollegeName(
                    user.getCollege().getInstituteName()
            );
        }

        // Branch
        if (user.getBranch() != null) {

            dto.setBranchId(
                    user.getBranch().getBranchId()
            );

            dto.setBranchName(
                    user.getBranch().getBranchName()
            );
        }

        // Section
        if (user.getSection() != null) {

            dto.setSectionId(
                    user.getSection().getSectionId()
            );

            dto.setSectionName(
                    user.getSection().getSectionName()
            );
        }

        // Role
        if (user.getRole() != null) {

            dto.setRoleId(
                    user.getRole().getRoleId()
            );

            dto.setRoleName(
                    user.getRole().getRoleName()
            );
        }

        dto.setGoogleId(user.getGoogleId());
        dto.setProfilePicture(user.getProfilePicture());

        dto.setLoginType(user.getLoginType());



        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }
}