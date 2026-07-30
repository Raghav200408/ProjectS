package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.mapper.BranchExcelMapper;
import com.project.ProjectS.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BranchExcelProcessor implements ExcelProcessor {

    @Autowired
    private BranchExcelMapper branchMapper;

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            if (row.values().stream().allMatch(String::isBlank)) {
                continue;
            }

            Branch branch = branchMapper.map(row);

            branchRepository.save(branch);
        }
    }
}