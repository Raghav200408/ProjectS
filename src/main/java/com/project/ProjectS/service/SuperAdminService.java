package com.project.ProjectS.service;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.SuperAdminDTO;
import com.project.ProjectS.repository.SuperAdminDAO;


@Service
public class SuperAdminService {

    @Autowired
    private SuperAdminDAO superAdminDAO;

    public void save(SuperAdminDTO dto) {
        superAdminDAO.save(dto);
    }

    public List<SuperAdminDTO> findAll() {

        return superAdminDAO.findAll();
    }

    public SuperAdminDTO findById(Integer id) {

        return superAdminDAO.findById(id);
    }

    public void update(Integer id, SuperAdminDTO dto) {

        superAdminDAO.update(id, dto);
    }
    public void delete(Integer id) {

        superAdminDAO.delete(id);
    }
}
