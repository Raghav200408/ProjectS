package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.TableHeader;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TableHeaderExcelMapper {

    public TableHeader map(Map<String, String> row) {

        TableHeader tableHeader = new TableHeader();

        // ExcelReader converts headers to lowercase
        String name = row.get("name");

        if (name != null && !name.trim().isEmpty()) {
            tableHeader.setName(name.trim());
        }

        // activeRow, rowStatus, createdAt, updatedAt
        // are handled by @PrePersist

        return tableHeader;
    }
}