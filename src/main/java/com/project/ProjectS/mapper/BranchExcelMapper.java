package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BranchExcelMapper implements ExcelRowMapper<Branch> {
    @Autowired
    public BranchExcelMapper(CollegeRepository collegeRepository) {
        this.collegeRepository = collegeRepository;
    }

    private final CollegeRepository collegeRepository;

    @Override
    public Branch map(Map<String, String> row) {

        List<College> colleges =
                collegeRepository.findByInstituteName(
                        row.get("college_name")
                );

        if (colleges.isEmpty()) {
            throw new RuntimeException(
                    "College not found : " + row.get("college_name")
            );
        }

        College college = colleges.get(0);

        Branch branch = new Branch();

        branch.setCollege(college);
        branch.setBranchName(row.get("branch_name"));
        branch.setAddress(row.get("address"));
        branch.setPhoneNumber(row.get("phone_number"));
        branch.setEmail(row.get("email"));

        return branch;
    }
}
