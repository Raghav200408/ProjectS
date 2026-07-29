package com.project.ProjectS.service;

import com.project.ProjectS.entity.QuestionCategory;
import com.project.ProjectS.model.QuestionCategoryRequestDTO;
import com.project.ProjectS.model.QuestionCategoryResponseDTO;
import com.project.ProjectS.repository.QuestionCategoryRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionCategoryService {

    @Autowired
    private QuestionCategoryRepository repository;

    private static final Logger logger =
            LogManager.getLogger(QuestionCategoryService.class);

    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;

    public String create(QuestionCategoryRequestDTO request) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Question Category already exists");
        }

        QuestionCategory category = new QuestionCategory();
        category.setName(request.getName());

        repository.save(category);

        return "Question Category created successfully";
    }

    public List<QuestionCategoryResponseDTO> getAll() {

        List<QuestionCategory> categories = repository.findAll();
        List<QuestionCategoryResponseDTO> response = new ArrayList<>();

        for (QuestionCategory category : categories) {

            QuestionCategoryResponseDTO dto = new QuestionCategoryResponseDTO();

            dto.setCategoryId(category.getCategoryId());
            dto.setName(category.getName());
            dto.setActiveRow(category.getActiveRow());
            dto.setRowStatus(category.getRowStatus());
            dto.setOrderOf(category.getOrderOf());
            dto.setCreatedAt(category.getCreatedAt());
            dto.setUpdatedAt(category.getUpdatedAt());

            response.add(dto);
        }

        return response;
    }

    public QuestionCategoryResponseDTO getById(Long id) {

        QuestionCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question Category not found"));

        QuestionCategoryResponseDTO dto = new QuestionCategoryResponseDTO();

        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setActiveRow(category.getActiveRow());
        dto.setRowStatus(category.getRowStatus());
        dto.setOrderOf(category.getOrderOf());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        return dto;
    }

    public String update(Long id, QuestionCategoryRequestDTO request) {

        QuestionCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question Category not found"));

        category.setName(request.getName());

        repository.save(category);

        return "Question Category updated successfully";
    }

    public String delete(Long id) {

        QuestionCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question Category not found"));

        repository.delete(category);

        return "Question Category deleted successfully";
    }

    public String uploadQuestionCategory(MultipartFile file) {

        logger.info("Starting question category Excel upload process.");

        if (file.isEmpty()) {

            throw new RuntimeException("File cannot be empty");

        }


        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {


            Sheet sheet = workbook.getSheetAt(0);


            boolean headerRow = true;


            int savedCount = 0;
            int skippedCount = 0;



            for (Row row : sheet) {


                // Skip header
                if (headerRow) {

                    headerRow = false;
                    continue;

                }



                String categoryName =
                        row.getCell(0)
                                .getStringCellValue()
                                .trim();



                Integer orderOf =
                        (int) row.getCell(1)
                                .getNumericCellValue();



                logger.info(
                        "Processing Question Category: {}",
                        categoryName
                );



                // Duplicate check

                if (questionCategoryRepository
                        .existsByName(categoryName)) {


                    logger.warn(
                            "Question Category already exists: {}",
                            categoryName
                    );


                    skippedCount++;

                    continue;

                }



                QuestionCategory category =
                        new QuestionCategory();



                category.setName(categoryName);

                category.setOrderOf(orderOf);



                questionCategoryRepository.save(category);



                savedCount++;


                logger.info(
                        "Question Category saved successfully: {}",
                        categoryName
                );

            }



            logger.info(
                    "Question Category upload completed. Saved: {}, Skipped: {}",
                    savedCount,
                    skippedCount
            );



            return "Question Category Excel upload completed. Saved: "
                    + savedCount
                    + ", Skipped: "
                    + skippedCount;



        }
        catch(Exception e) {


            logger.error(
                    "Failed while uploading Question Category Excel",
                    e
            );


            throw new RuntimeException(
                    "Failed to upload Excel file"
            );

        }

    }
}