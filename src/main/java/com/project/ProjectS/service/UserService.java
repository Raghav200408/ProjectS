package com.project.ProjectS.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.repository.UserDAO;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    // ==========================
    // Normal Registration
    // ==========================

    public void save(UserDTO dto) {

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

        userDAO.update(id, dto);

    }

    public void delete(Integer id) {

        userDAO.delete(id);

    }

}