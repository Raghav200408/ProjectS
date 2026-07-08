package com.project.ProjectS.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.BranchDTO;
import com.project.ProjectS.repository.BranchDAO;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;



@Service
public class BranchService {

    @Autowired
    private BranchDAO branchDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Save Branch
 // Save Branch
    public BranchDTO saveBranch(BranchDTO branch) {

        BranchDTO savedBranch = branchDAO.save(branch);

        return getBranchById(savedBranch.getBranchId());
    }

    // Get All Branches
    public List<BranchDTO> getAllBranches() {

        String sql = """
            SELECT
                b.branch_id,
                b.college_id,
                c.institute_name,
                b.branch_name,
                b.address,
                b.phone_number,
                b.email
            FROM branch b
            INNER JOIN college c
            ON b.college_id = c.college_id
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            BranchDTO branch = new BranchDTO();

            branch.setBranchId(rs.getLong("branch_id"));
            branch.setCollegeId(rs.getLong("college_id"));
            branch.setCollegeName(rs.getString("institute_name"));
            branch.setBranchName(rs.getString("branch_name"));
            branch.setAddress(rs.getString("address"));
            branch.setPhoneNumber(rs.getString("phone_number"));
            branch.setEmail(rs.getString("email"));

            return branch;
        });
    }

    // Get Branch By Id
    public BranchDTO getBranchById(Long id) {

        String sql = """
            SELECT
                b.branch_id,
                b.college_id,
                c.institute_name AS college_name,
                b.branch_name,
                b.address,
                b.phone_number,
                b.email
            FROM branch b
            INNER JOIN college c
                ON b.college_id = c.college_id
            WHERE b.branch_id = ?
            """;

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {

            BranchDTO branch = new BranchDTO();

            branch.setBranchId(rs.getLong("branch_id"));
            branch.setCollegeId(rs.getLong("college_id"));
            branch.setCollegeName(rs.getString("college_name"));
            branch.setBranchName(rs.getString("branch_name"));
            branch.setAddress(rs.getString("address"));
            branch.setPhoneNumber(rs.getString("phone_number"));
            branch.setEmail(rs.getString("email"));

            return branch;
        });
    }

    // Update Branch
    	public BranchDTO updateBranch(BranchDTO branch) {

    	    branchDAO.save(branch);

    	    return getBranchById(branch.getBranchId());
    	}

    // Delete Branch
    public void deleteBranch(Long id) {
        branchDAO.deleteById(id);
    }
}