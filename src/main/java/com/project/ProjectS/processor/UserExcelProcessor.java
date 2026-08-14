package com.project.ProjectS.processor;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.mapper.UserExcelMapper;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserExcelProcessor implements ExcelProcessor {
    @Autowired
    public UserExcelProcessor(UserExcelMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    private final UserExcelMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            // Skip empty rows
            if (row.values().stream().allMatch(String::isBlank)) {
                continue;
            }

            // Map Excel row to User
            User user = userMapper.map(row);

            // Save user
            userRepository.save(user);
        }
    }
}
