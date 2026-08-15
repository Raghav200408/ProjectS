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
    public ChapterExcelProcessor(ChapterExcelMapper chapterExcelMapper, ChapterRepository chapterRepository, GenericExcelUploadService genericExcelUploadService) {
        this.chapterExcelMapper = chapterExcelMapper;
        this.chapterRepository = chapterRepository;
        this.genericExcelUploadService = genericExcelUploadService;
    }

    private final ChapterExcelMapper chapterExcelMapper;
    private final ChapterRepository chapterRepository;
    private final GenericExcelUploadService genericExcelUploadService;



    public void process(List<Map<String,String>> excelData) {


        genericExcelUploadService.process(
                excelData,
                chapterExcelMapper,
                chapterRepository
        );

    }
}
