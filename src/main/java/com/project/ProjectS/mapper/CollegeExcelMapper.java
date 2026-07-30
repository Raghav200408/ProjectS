package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.College;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class CollegeExcelMapper implements ExcelRowMapper<College> {

    @Override
    public College map(Map<String, String> row) {

        College college = new College();

        college.setInstituteName(row.get("institute_name"));
        college.setAddress(row.get("address"));
        college.setPhoneNumber(row.get("phone_number"));
        college.setEmail(row.get("email_address"));

        return college;
    }
}