package com.project.ProjectS.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.LoginRequestDTO;
import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.repository.UserDAO;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================
    // Normal Registration
    // ==========================

    public void save(UserDTO dto) {

        dto.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        userDAO.save(dto);

    }

    // ==========================
    // Google Registration
    // ==========================

    public void registerGoogleUser(UserDTO dto) {

        userDAO.saveGoogleUser(dto);

    }

    // ==========================
    // Email Check
    // ==========================

    public boolean existsByEmail(String email) {

        return userDAO.existsByEmail(email);

    }

    public UserDTO findByEmail(String email) {

        return userDAO.findByEmail(email);

    }
    public UserDTO findOrCreateGoogleUser(String name, String email) {

        UserDTO user = userDAO.findByEmail(email);

        if (user != null) {
            return user;
        }

        UserDTO newUser = new UserDTO();

        newUser.setName(name);
        newUser.setEmail(email);

        // Default role for Google users
        newUser.setRoleId(3);

        newUser.setPhoneNumber(null);
        newUser.setAddress(null);
        newUser.setCollege(null);
        newUser.setBranch(null);
        newUser.setClassName(null);
        newUser.setSection(null);
        newUser.setStudentCode(null);
        newUser.setEmployeeId(null);
        newUser.setGuardianName(null);
        newUser.setGuardianPhoneNumber(null);

        userDAO.saveGoogleUser(newUser);

        return userDAO.findByEmail(email);
    }
    // ==========================
    // User CRUD
    // ==========================

    public List<UserDTO> findAll() {

        return userDAO.findAll();

    }

    public UserDTO findById(Integer id) {

        return userDAO.findById(id);

    }

    public void update(Integer id, UserDTO dto) {

        dto.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        userDAO.update(id, dto);

    }

    public void delete(Integer id) {

        userDAO.delete(id);

    }
    
    

}