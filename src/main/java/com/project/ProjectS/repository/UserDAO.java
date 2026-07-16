package com.project.ProjectS.repository;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.project.ProjectS.model.UserDTO;

@Repository
public class UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    
    public int save(UserDTO dto) {

        String sql = "INSERT INTO users("
                + "name,designation,address,employee_id,"
                + "student_code,class_name,college,branch,section,"
                + "email,phone_number,password,"
                + "guardian_name,guardian_phone_number,role_id, login_type)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        return jdbcTemplate.update(sql,
                dto.getName(),
                dto.getDesignation(),
                dto.getAddress(),
                dto.getEmployeeId(),
                dto.getStudentCode(),
                dto.getClassName(),
                dto.getCollege(),
                dto.getBranch(),
                dto.getSection(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getPassword(),
                dto.getGuardianName(),
                dto.getGuardianPhoneNumber(),
                dto.getRoleId(),
                "NORMAL" 
                );
    }

  
    public List<UserDTO> findAll() {

        String sql = """
                SELECT u.user_id,
                       u.name,
                       u.designation,
                       u.address,
                       u.employee_id,
                       u.student_code,
                       u.class_name,
                       u.college,
                       u.branch,
                       u.section,
                       u.email,
                       u.phone_number,
                       u.password,
                       u.guardian_name,
                       u.guardian_phone_number,
                       u.role_id,
                       r.role_name
                FROM users u
                JOIN roles r
                ON u.role_id=r.role_id
                ORDER BY u.user_id
                """;

        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(UserDTO.class));
    }


    public UserDTO findById(Integer id) {

        String sql = """
                SELECT u.user_id,
                       u.name,
                       u.designation,
                       u.address,
                       u.employee_id,
                       u.student_code,
                       u.class_name,
                       u.college,
                       u.branch,
                       u.section,
                       u.email,
                       u.phone_number,
                       u.password,
                       u.guardian_name,
                       u.guardian_phone_number,
                       u.role_id,
                       r.role_name
                FROM users u
                JOIN roles r
                ON u.role_id=r.role_id
                WHERE user_id=?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(UserDTO.class),
                id);
    }

    // Update User
    public int update(Integer id, UserDTO dto) {

        String sql = """
                UPDATE users
                SET name=?,
                    designation=?,
                    address=?,
                    employee_id=?,
                    student_code=?,
                    class_name=?,
                    college=?,
                    branch=?,
                    section=?,
                    email=?,
                    phone_number=?,
                    password=?,
                    guardian_name=?,
                    guardian_phone_number=?,
                    role_id=?
                WHERE user_id=?
                """;

        return jdbcTemplate.update(sql,
                dto.getName(),
                dto.getDesignation(),
                dto.getAddress(),
                dto.getEmployeeId(),
                dto.getStudentCode(),
                dto.getClassName(),
                dto.getCollege(),
                dto.getBranch(),
                dto.getSection(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getPassword(),
                dto.getGuardianName(),
                dto.getGuardianPhoneNumber(),
                dto.getRoleId(),
                id);
    }

    // Delete User
    public int delete(Integer id) {

        String sql = "DELETE FROM users WHERE user_id=?";

        return jdbcTemplate.update(sql, id);
    }
    public UserDTO findByEmail(String email) {

        String sql = """
                SELECT u.user_id,
                       u.name,
                       u.designation,
                       u.address,
                       u.employee_id,
                       u.student_code,
                       u.class_name,
                       u.college,
                       u.branch,
                       u.section,
                       u.email,
                       u.phone_number,
                       u.password,
                       u.guardian_name,
                       u.guardian_phone_number,
                       u.role_id,
                       r.role_name
                FROM users u
                JOIN roles r
                ON u.role_id = r.role_id
                WHERE u.email = ?
                """;

        try {

            return jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(UserDTO.class),
                    email);

        } catch (Exception e) {

            return null;

        }

    }
    public boolean existsByEmail(String email) {

        String sql = "SELECT COUNT(*) FROM users WHERE email=?";

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                email);

        return count != null && count > 0;
    }
    public int saveGoogleUser(UserDTO dto) {

        String sql = """
                INSERT INTO users
(
    name,
    email,
    role_id,
    phone_number,
    address,
    college,
    branch,
    class_name,
    section,
    student_code,
    employee_id,
    guardian_name,
    guardian_phone_number,
    login_type
)
VALUES
(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        return jdbcTemplate.update(
                sql,
                dto.getName(),
                dto.getEmail(),
                dto.getRoleId(),
                dto.getPhoneNumber(),
                dto.getAddress(),
                dto.getCollege(),
                dto.getBranch(),
                dto.getClassName(),
                dto.getSection(),
                dto.getStudentCode(),
                dto.getEmployeeId(),
                dto.getGuardianName(),
                dto.getGuardianPhoneNumber(),
                "GOOGLE"
        );

    }
   
   
}
