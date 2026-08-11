package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.repository.TableHeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TableAttributeExcelMapper {

    private final TableHeaderRepository tableHeaderRepository;

    public TableAttribute map(Map<String, String> row) {

        TableAttribute attribute = new TableAttribute();

        String name = row.get("name");
        String headerName = row.get("header_name");
        String amount1 = row.get("amount1");
        String amount2 = row.get("amount2");

        // Row Disable
        String rowDisable = row.get("row_disable");

        if (name != null && !name.trim().isEmpty()) {
            attribute.setName(name.trim());
        }

        if (amount1 != null && !amount1.isBlank()) {
            attribute.setAmount1(Long.parseLong(amount1));
        }

        if (amount2 != null && !amount2.isBlank()) {
            attribute.setAmount2(Long.parseLong(amount2));
        }

        // Column Disable
        if (rowDisable != null && !rowDisable.isBlank()) {

            attribute.setRowDisable(
                    Boolean.parseBoolean(rowDisable.trim())
            );

        } else {

            attribute.setRowDisable(false);
        }

        if (headerName != null && !headerName.trim().isEmpty()) {

            TableHeader header = tableHeaderRepository.findByName(headerName.trim())
                    .orElseThrow(() ->
                            new RuntimeException("Header not found: " + headerName));

            attribute.setTableHeader(header);
        }

        return attribute;
    }
}