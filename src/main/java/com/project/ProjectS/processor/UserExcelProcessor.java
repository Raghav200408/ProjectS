package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Role;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.mapper.UserExcelMapper;
import com.project.ProjectS.repository.RoleRepository;
import com.project.ProjectS.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class UserExcelProcessor implements ExcelProcessor {


    @Autowired
    private UserExcelMapper userMapper;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private RoleRepository roleRepository;



    @Override
    public void process(List<Map<String, String>> excelData) {


        // Default role for Excel upload
        Role studentRole =
                roleRepository.findByRoleName("STUDENT")
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "STUDENT role not found"
                                )
                        );


        for (Map<String, String> row : excelData) {


            // Skip empty rows
            if (row.values().stream().allMatch(String::isBlank)) {
                continue;
            }


            User user = userMapper.map(row);


            // Assign default role
            user.setRole(studentRole);


            userRepository.save(user);
        }
    }
}