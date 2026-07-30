package com.project.ProjectS.mapper;

import java.util.Map;

@FunctionalInterface
public interface ExcelRowMapper<T> {

    T map(Map<String,String> row);

}