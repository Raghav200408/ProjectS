package com.project.ProjectS.service;

import com.project.ProjectS.util.ExcelReader;
import com.project.ProjectS.util.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelUploadService {


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


        // Step 1: Validate file
        fileValidator.validate(file);



        // Step 2: Read Excel
        return excelReader.readExcel(file);

    }

}