package com.project.ProjectS.processor;

import com.project.ProjectS.mapper.QuestionCategoryExcelMapper;
import com.project.ProjectS.repository.QuestionCategoryRepository;
import com.project.ProjectS.service.GenericExcelUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class QuestionCategoryExcelProcessor {


    @Autowired
    private QuestionCategoryExcelMapper questionCategoryExcelMapper;


    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;


    @Autowired
    private GenericExcelUploadService genericExcelUploadService;



    public void process(List<Map<String,String>> excelData) {


        genericExcelUploadService.process(
                excelData,
                questionCategoryExcelMapper,
                questionCategoryRepository
        );

    }

}