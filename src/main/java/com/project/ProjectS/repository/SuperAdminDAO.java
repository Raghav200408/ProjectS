package com.project.ProjectS.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.project.ProjectS.model.SuperAdminDTO;
@Repository
public class SuperAdminDAO {
	  @Autowired
	   private JdbcTemplate jdbcTemplate;
	
	public int  save(SuperAdminDTO dto) {
		String sql ="INSERT INTO super_admin "
                + "(name, designation, address, employee_id, email, phone_number, password, role_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		return jdbcTemplate.update(sql,
                dto.getName(),
                dto.getDesignation(),
                dto.getAddress(),
                dto.getEmployeeId(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getPassword(),
                dto.getRoleId());
	
		
	}
	
	    public List<SuperAdminDTO> findAll() {

	        String sql = "SELECT * FROM super_admin ORDER BY super_admin_id";

	        return jdbcTemplate.query(
	                sql,
	                new BeanPropertyRowMapper<>(SuperAdminDTO.class));
	    }

	 
	    public SuperAdminDTO findById(Integer id) {

	        String sql = "SELECT * FROM super_admin WHERE super_admin_id = ?";

	        return jdbcTemplate.queryForObject(
	                sql,
	                new BeanPropertyRowMapper<>(SuperAdminDTO.class),
	                id);
	    }

	    
	    public int update(Integer id, SuperAdminDTO dto) {

	        String sql = "UPDATE super_admin SET "
	                + "name=?, "
	                + "designation=?, "
	                + "address=?, "
	                + "employee_id=?, "
	                + "email=?, "
	                + "phone_number=?, "
	                + "password=?, "
	                + "role_id=? "
	                + "WHERE super_admin_id=?";

	        return jdbcTemplate.update(sql,
	                dto.getName(),
	                dto.getDesignation(),
	                dto.getAddress(),
	                dto.getEmployeeId(),
	                dto.getEmail(),
	                dto.getPhoneNumber(),
	                dto.getPassword(),
	                dto.getRoleId(),
	                id);
	    }

	
	    public int delete(Integer id) {

	        String sql = "DELETE FROM super_admin WHERE super_admin_id = ?";

	        return jdbcTemplate.update(sql, id);
	    }

}
