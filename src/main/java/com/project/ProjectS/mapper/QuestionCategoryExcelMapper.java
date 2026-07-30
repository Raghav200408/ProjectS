package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.QuestionCategory;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class QuestionCategoryExcelMapper
        implements ExcelRowMapper<QuestionCategory> {


    @Override
    public QuestionCategory map(
            Map<String,String> row) {


        String name =
                row.get("name");


        String order =
                row.get("order_of");


        QuestionCategory category =
                new QuestionCategory();


        category.setName(name);


        if(order != null && !order.isBlank()) {

            category.setOrderOf(
                    Integer.parseInt(order)
            );

        }


        return category;
    }
}