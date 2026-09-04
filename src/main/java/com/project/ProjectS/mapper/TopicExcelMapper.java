package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Topic;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TopicExcelMapper
        implements ExcelRowMapper<Topic> {

    @Override
    public Topic map(Map<String, String> row) {

        Topic topic =
                new Topic();

        // name
        String name = row.get("name");

        if (name != null && !name.isBlank()) {
            topic.setName(name.trim());
        }

        // active_row
        String activeRow =
                row.get("active_row");

        if (activeRow != null &&
                !activeRow.isBlank()) {

            topic.setActiveRow(
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

            topic.setRowStatus(
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

            topic.setOrderOf(
                    Integer.parseInt(
                            orderOf.trim()
                    )
            );
        }

        return topic;
    }
}