package com.project.ProjectS.service;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.QuestionCategory;
import com.project.ProjectS.model.QuestionCategoryRequestDTO;
import com.project.ProjectS.model.QuestionCategoryResponseDTO;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.QuestionCategoryRepository;
import com.project.ProjectS.processor.QuestionCategoryExcelProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionCategoryService {

    @Autowired
    private QuestionCategoryRepository repository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private QuestionCategoryExcelProcessor questionCategoryExcelProcessor;

    private static final Logger logger =
            LogManager.getLogger(QuestionCategoryService.class);


    // =========================================================
    // CREATE
    // =========================================================

    public String create(QuestionCategoryRequestDTO request) {

        logger.info(
                "Creating question category with name: {}",
                request.getName()
        );

        Course course = courseRepository.findById(
                request.getCourseId()
        ).orElseThrow(() -> {

            logger.warn(
                    "Course not found with ID: {}",
                    request.getCourseId()
            );

            return new RuntimeException(
                    "Course not found"
            );
        });


        Chapter chapter = chapterRepository.findById(
                request.getChapterId()
        ).orElseThrow(() -> {

            logger.warn(
                    "Chapter not found with ID: {}",
                    request.getChapterId()
            );

            return new RuntimeException(
                    "Chapter not found"
            );
        });


        // Duplicate check based on
        // Course + Chapter + Category Name

        if (repository.existsByCourseAndChapterAndName(
                course,
                chapter,
                request.getName()
        )) {

            logger.warn(
                    "Question Category already exists: {}",
                    request.getName()
            );

            throw new RuntimeException(
                    "Question Category already exists"
            );
        }


        QuestionCategory category =
                new QuestionCategory();

        category.setCourse(course);
        category.setChapter(chapter);
        category.setName(request.getName());
        category.setActiveRow(request.getActiveRow());


        repository.save(category);


        logger.info(
                "Question Category created successfully with name: {}",
                request.getName()
        );

        return "Question Category created successfully";
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<QuestionCategoryResponseDTO> getAll() {

        List<QuestionCategory> categories =
                repository.findAll();

        List<QuestionCategoryResponseDTO> response =
                new ArrayList<>();


        for (QuestionCategory category : categories) {

            QuestionCategoryResponseDTO dto =
                    new QuestionCategoryResponseDTO();


            dto.setCategoryId(
                    category.getCategoryId()
            );


            if (category.getCourse() != null) {

                dto.setCourseId(
                        category.getCourse().getCourseId()
                );

                dto.setCourseName(
                        category.getCourse().getName()
                );
            }


            if (category.getChapter() != null) {

                dto.setChapterId(
                        category.getChapter().getChapterId()
                );

                dto.setChapterName(
                        category.getChapter().getName()
                );
            }


            dto.setName(
                    category.getName()
            );

            dto.setActiveRow(
                    category.getActiveRow()
            );

            dto.setRowStatus(
                    category.getRowStatus()
            );

            dto.setOrderOf(
                    category.getOrderOf()
            );

            dto.setCreatedAt(
                    category.getCreatedAt()
            );

            dto.setUpdatedAt(
                    category.getUpdatedAt()
            );


            response.add(dto);
        }

        return response;
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public QuestionCategoryResponseDTO getById(Long id) {

        QuestionCategory category =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question Category not found"
                                )
                        );


        QuestionCategoryResponseDTO dto =
                new QuestionCategoryResponseDTO();


        dto.setCategoryId(
                category.getCategoryId()
        );


        if (category.getCourse() != null) {

            dto.setCourseId(
                    category.getCourse().getCourseId()
            );

            dto.setCourseName(
                    category.getCourse().getName()
            );
        }


        if (category.getChapter() != null) {

            dto.setChapterId(
                    category.getChapter().getChapterId()
            );

            dto.setChapterName(
                    category.getChapter().getName()
            );
        }


        dto.setName(
                category.getName()
        );

        dto.setActiveRow(
                category.getActiveRow()
        );

        dto.setRowStatus(
                category.getRowStatus()
        );

        dto.setOrderOf(
                category.getOrderOf()
        );

        dto.setCreatedAt(
                category.getCreatedAt()
        );

        dto.setUpdatedAt(
                category.getUpdatedAt()
        );


        return dto;
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public String update(
            Long id,
            QuestionCategoryRequestDTO request) {

        logger.info(
                "Updating Question Category with ID: {}",
                id
        );


        QuestionCategory category =
                repository.findById(id)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Question Category not found with ID: {}",
                                    id
                            );

                            return new RuntimeException(
                                    "Question Category not found"
                            );
                        });


        Course course =
                courseRepository.findById(
                        request.getCourseId()
                ).orElseThrow(() -> {

                    logger.warn(
                            "Course not found with ID: {}",
                            request.getCourseId()
                    );

                    return new RuntimeException(
                            "Course not found"
                    );
                });


        Chapter chapter =
                chapterRepository.findById(
                        request.getChapterId()
                ).orElseThrow(() -> {

                    logger.warn(
                            "Chapter not found with ID: {}",
                            request.getChapterId()
                    );

                    return new RuntimeException(
                            "Chapter not found"
                    );
                });


        // Check duplicate only when another category
        // with the same Course + Chapter + Name exists

        QuestionCategory existing =
                repository.findByCourseAndChapterAndName(
                        course,
                        chapter,
                        request.getName()
                ).orElse(null);


        if (existing != null &&
                !existing.getCategoryId().equals(id)) {

            throw new RuntimeException(
                    "Question Category already exists"
            );
        }


        category.setCourse(course);
        category.setChapter(chapter);
        category.setName(request.getName());
        category.setActiveRow(request.getActiveRow());


        repository.save(category);


        logger.info(
                "Question Category updated successfully with ID: {}",
                id
        );

        return "Question Category updated successfully";
    }


    // =========================================================
    // DELETE
    // =========================================================

    public String delete(Long id) {

        QuestionCategory category =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question Category not found"
                                )
                        );


        repository.delete(category);


        return "Question Category deleted successfully";
    }


    // =========================================================
    // EXCEL UPLOAD
    // =========================================================

    public String uploadQuestionCategory(
            MultipartFile file) {

        logger.info(
                "Starting Question Category Excel upload process."
        );


        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "File cannot be empty"
            );
        }


        try {

            // Read Excel using your existing
            // generic Excel reader/flow.
            //
            // The controller/service that currently
            // converts MultipartFile -> List<Map<String,String>>
            // should call the processor.

            logger.info(
                    "Question Category Excel file received: {}",
                    file.getOriginalFilename()
            );

            /*
             * IMPORTANT:
             *
             * Your current architecture has:
             *
             * Excel
             *   ↓
             * ExcelReader
             *   ↓
             * List<Map<String,String>>
             *   ↓
             * QuestionCategoryExcelProcessor
             *
             * Therefore the old XSSFWorkbook code should
             * NOT be kept here.
             */


            return "Question Category Excel upload request received successfully";

        } catch (Exception e) {

            logger.error(
                    "Failed while uploading Question Category Excel",
                    e
            );

            throw new RuntimeException(
                    "Failed to upload Excel file: "
                            + e.getMessage()
            );
        }
    }
}