package com.project.ProjectS.processor;

import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.mapper.TableNameExcelMapper;
import com.project.ProjectS.repository.TableNameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TableNameExcelProcessor implements ExcelProcessor {

    private final TableNameRepository repository;
    private final TableNameExcelMapper mapper;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            TableName tableName = mapper.map(row);

            if (tableName.getName() == null || tableName.getName().isBlank()) {
                continue;
            }

            if (repository.existsByName(tableName.getName())) {
                continue;
            }

            repository.save(tableName);
        }
    }
}