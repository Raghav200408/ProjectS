package com.project.ProjectS.processor;

import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.mapper.TableHeaderExcelMapper;
import com.project.ProjectS.repository.TableHeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TableHeaderExcelProcessor implements ExcelProcessor {

    private final TableHeaderRepository repository;
    private final TableHeaderExcelMapper mapper;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            TableHeader tableHeader = mapper.map(row);

            if (tableHeader.getName() == null || tableHeader.getName().isBlank()) {
                continue;
            }

            if (repository.existsByName(tableHeader.getName())) {
                continue;
            }

            repository.save(tableHeader);
        }
    }
}