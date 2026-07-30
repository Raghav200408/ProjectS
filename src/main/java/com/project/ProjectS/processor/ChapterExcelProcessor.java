package com.project.ProjectS.processor;


import com.project.ProjectS.mapper.ChapterExcelMapper;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.service.GenericExcelUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class ChapterExcelProcessor {


    @Autowired
    private ChapterExcelMapper chapterExcelMapper;


    @Autowired
    private ChapterRepository chapterRepository;


    @Autowired
    private GenericExcelUploadService genericExcelUploadService;



    public void process(List<Map<String,String>> excelData) {


        genericExcelUploadService.process(
                excelData,
                chapterExcelMapper,
                chapterRepository
        );

    }
}