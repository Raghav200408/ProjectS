package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.TableName;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TableNameExcelMapper {

    public TableName map(Map<String, String> row) {

        TableName tableName = new TableName();

        String name = row.get("name");

        if (name != null) {
            tableName.setName(name.trim());
        }

        // Do NOT set:
        // activeRow
        // rowStatus
        // createdAt
        // updatedAt
        // They are handled by @PrePersist.

        return tableName;
    }
}