package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.QuestionCategory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QuestionCategoryExcelMapper
        implements ExcelRowMapper<QuestionCategory> {

    @Override
    public QuestionCategory map(Map<String, String> row) {

        QuestionCategory category =
                new QuestionCategory();

        // name
        String name = row.get("name");

        if (name != null && !name.isBlank()) {
            category.setName(name.trim());
        }

        // active_row
        String activeRow =
                row.get("active_row");

        if (activeRow != null &&
                !activeRow.isBlank()) {

            category.setActiveRow(
                    Boolean.parseBoolean(
                            activeRow.trim()
                    )
            );
        }

        // row_status
        String rowStatus =
                row.get("row_status");

        if (rowStatus != null &&
                !rowStatus.isBlank()) {

            category.setRowStatus(
                    Integer.parseInt(
                            rowStatus.trim()
                    )
            );
        }

        // order_of
        String orderOf =
                row.get("order_of");

        if (orderOf != null &&
                !orderOf.isBlank()) {

            category.setOrderOf(
                    Integer.parseInt(
                            orderOf.trim()
                    )
            );
        }

        return category;
    }
}