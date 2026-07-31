package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserExcelMapper implements ExcelRowMapper<User> {


    @Override
    public User map(Map<String, String> row) {

        User user = new User();

        user.setName(row.get("name"));

        user.setEmail(row.get("email"));

        user.setPhoneNumber(row.get("phone_number"));

        user.setPassword(row.get("password"));

        user.setAddress(row.get("address"));

        return user;
    }
}