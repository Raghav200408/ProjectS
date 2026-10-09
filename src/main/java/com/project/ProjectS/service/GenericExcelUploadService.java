package com.project.ProjectS.service;

import com.project.ProjectS.mapper.ExcelRowMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class GenericExcelUploadService {
    private static final Logger log = LogManager.getLogger(GenericExcelUploadService.class);


    @Transactional
    public <T> void process(
            List<Map<String, String>> excelData,
            ExcelRowMapper<T> mapper,
            JpaRepository<T, Long> repository) {


        List<T> entities = new ArrayList<>();


        for (Map<String, String> row : excelData) {


            // skip empty rows

            if(row.values()
                    .stream()
                    .allMatch(String::isBlank)) {

                continue;
            }



            T entity =
                    mapper.map(row);



            if(entity != null) {

                entities.add(entity);

            }

        }


        repository.saveAll(entities);

    }

}