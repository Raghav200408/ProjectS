package com.project.ProjectS.processor;

import com.project.ProjectS.entity.College;
import com.project.ProjectS.mapper.CollegeExcelMapper;
import com.project.ProjectS.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CollegeExcelProcessor implements ExcelProcessor {

    @Autowired
    private CollegeExcelMapper collegeMapper;

    @Autowired
    private CollegeRepository collegeRepository;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            if (row.values().stream().allMatch(String::isBlank)) {
                continue;
            }

            College college = collegeMapper.map(row);

            collegeRepository.save(college);
        }
    }
}