package com.project.ProjectS.service;

import com.project.ProjectS.util.ExcelReader;
import com.project.ProjectS.util.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelUploadService {
    private static final Logger log = LogManager.getLogger(ExcelUploadService.class);


    private final ExcelReader excelReader;

    private final FileValidator fileValidator;


    @Autowired
    public ExcelUploadService(
            ExcelReader excelReader,
            FileValidator fileValidator) {

        this.excelReader = excelReader;
        this.fileValidator = fileValidator;
    }



    public List<Map<String,String>> readExcel(
            MultipartFile file) throws IOException {
        log.info("Reading Excel upload: originalFilename={} size={}",
                file == null ? null : file.getOriginalFilename(), file == null ? 0 : file.getSize());

        // Step 1: Validate file
        fileValidator.validate(file);



        // Step 2: Read Excel
        return excelReader.readExcel(file);

    }

}