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
import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;

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

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    public String create(QuestionCategoryRequestDTO request) {

        logger.info("Creating question category with name: {}", request.getName());

        if (repository.existsByName(request.getName())) {
            logger.warn("Question Category already exists with name: {}", request.getName());
            throw new RuntimeException("Question Category already exists");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", request.getCourseId());
                    return new RuntimeException("Course not found");
                });

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> {
                    logger.warn("Chapter not found with ID: {}", request.getChapterId());
                    return new RuntimeException("Chapter not found");
                });

        QuestionCategory category = new QuestionCategory();

        category.setCourse(course);
        category.setChapter(chapter);
        category.setName(request.getName());
        category.setActiveRow(request.getActiveRow());

        repository.save(category);

        logger.info("Question Category created successfully with name: {}", request.getName());

        return "Question Category created successfully";
    }

    public List<QuestionCategoryResponseDTO> getAll() {

        List<QuestionCategory> categories = repository.findAll();
        List<QuestionCategoryResponseDTO> response = new ArrayList<>();

        for (QuestionCategory category : categories) {

            QuestionCategoryResponseDTO dto = new QuestionCategoryResponseDTO();

            dto.setCategoryId(category.getCategoryId());

            dto.setCourseId(category.getCourse().getCourseId());
            dto.setCourseName(category.getCourse().getName());

            dto.setChapterId(category.getChapter().getChapterId());
            dto.setChapterName(category.getChapter().getName());

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

        dto.setCourseId(category.getCourse().getCourseId());
        dto.setCourseName(category.getCourse().getName());

        dto.setChapterId(category.getChapter().getChapterId());
        dto.setChapterName(category.getChapter().getName());

        dto.setName(category.getName());

        dto.setActiveRow(category.getActiveRow());
        dto.setRowStatus(category.getRowStatus());
        dto.setOrderOf(category.getOrderOf());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        return dto;
    }

    public String update(Long id, QuestionCategoryRequestDTO request) {

        logger.info("Updating Question Category with ID: {}", id);

        QuestionCategory category = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Question Category not found with ID: {}", id);
                    return new RuntimeException("Question Category not found");
                });

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", request.getCourseId());
                    return new RuntimeException("Course not found");
                });

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> {
                    logger.warn("Chapter not found with ID: {}", request.getChapterId());
                    return new RuntimeException("Chapter not found");
                });

        category.setCourse(course);
        category.setChapter(chapter);
        category.setName(request.getName());
        category.setActiveRow(request.getActiveRow());

        repository.save(category);

        logger.info("Question Category updated successfully with ID: {}", id);

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