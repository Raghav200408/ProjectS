package com.project.ProjectS.processor;

import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.mapper.TableAttributeExcelMapper;
import com.project.ProjectS.repository.TableAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TableAttributeExcelProcessor implements ExcelProcessor {

    private final TableAttributeRepository repository;
    private final TableAttributeExcelMapper mapper;

    @Override
    public void process(List<Map<String, String>> excelData) {

        for (Map<String, String> row : excelData) {

            TableAttribute attribute = mapper.map(row);

            // Skip empty name
            if (attribute.getName() == null || attribute.getName().isBlank()) {
                continue;
            }

            // Skip if Table Header is not found
            if (attribute.getTableHeader() == null) {
                continue;
            }

            // Skip duplicate Attribute under the same Header
            if (repository.existsByNameAndTableHeader(
                    attribute.getName(),
                    attribute.getTableHeader())) {
                continue;
            }

            repository.save(attribute);
        }
    }
}