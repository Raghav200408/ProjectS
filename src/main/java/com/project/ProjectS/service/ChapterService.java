package com.project.ProjectS.service;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.model.ChapterRequestDTO;
import com.project.ProjectS.model.ChapterResponseDTO;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    private static final Logger logger =
            LogManager.getLogger(ChapterService.class);

    @Autowired
    private ChapterRepository chapterRepository;


    @Autowired
    private CourseRepository courseRepository;

    public String create(ChapterRequestDTO request) {

        logger.info("Creating chapter with name: {}", request.getName());

        if (chapterRepository.existsByName(request.getName())) {
            logger.warn("Chapter already exists with name: {}", request.getName());
            throw new RuntimeException("Chapter already exists");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", request.getCourseId());
                    return new RuntimeException("Course not found");
                });

        Chapter entity = new Chapter();

        entity.setCourse(course);
        entity.setName(request.getName());

        chapterRepository.save(entity);

        logger.info("Chapter created successfully with name: {}", request.getName());

        return "Chapter created successfully";
    }

    public List<ChapterResponseDTO> getAll() {

        logger.info("Fetching all chapters.");

        List<ChapterResponseDTO> chapters = chapterRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        logger.info("Fetched {} chapters.", chapters.size());

        return chapters;
    }

    public ChapterResponseDTO getById(Long id) {

        logger.info("Fetching chapter with ID: {}", id);

        Chapter entity = chapterRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Chapter not found with ID: {}", id);
                    return new RuntimeException("Chapter not found");
                });

        logger.info("Chapter fetched successfully with ID: {}", id);

        return convert(entity);
    }

    public String update(Long id, ChapterRequestDTO request) {

        logger.info("Updating chapter with ID: {}", id);

        Chapter entity = chapterRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Chapter not found with ID: {}", id);
                    return new RuntimeException("Chapter not found");
                });

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", request.getCourseId());
                    return new RuntimeException("Course not found");
                });

        entity.setCourse(course);
        entity.setName(request.getName());

        chapterRepository.save(entity);

        logger.info("Chapter updated successfully with ID: {}", id);

        return "Chapter updated successfully";
    }

    public String delete(Long id) {

        logger.info("Deleting chapter with ID: {}", id);

        Chapter entity = chapterRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Chapter not found with ID: {}", id);
                    return new RuntimeException("Chapter not found");
                });

        chapterRepository.delete(entity);

        logger.info("Chapter deleted successfully with ID: {}", id);

        return "Chapter deleted successfully";
    }

    private ChapterResponseDTO convert(Chapter entity) {

        ChapterResponseDTO dto = new ChapterResponseDTO();

        dto.setChapterId(entity.getChapterId());

        dto.setCourseId(entity.getCourse().getCourseId());
        dto.setCourseName(entity.getCourse().getName());

        dto.setName(entity.getName());

        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public String uploadChapter(MultipartFile file) {


        logger.info("Starting chapter Excel upload process.");


        if(file.isEmpty()){

            throw new RuntimeException(
                    "File cannot be empty"
            );

        }



        try(InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream)){



            Sheet sheet = workbook.getSheetAt(0);


            boolean headerRow = true;


            int savedCount = 0;
            int skippedCount = 0;



            for(Row row : sheet){



                if(headerRow){

                    headerRow = false;
                    continue;

                }



                String courseName =
                        row.getCell(0)
                                .getStringCellValue()
                                .trim();



                String chapterName =
                        row.getCell(1)
                                .getStringCellValue()
                                .trim();




                Course course =
                        courseRepository.findByName(courseName)
                                .orElseThrow(() ->

                                        new RuntimeException(
                                                "Course not found : "
                                                        + courseName
                                        )
                                );




                if(chapterRepository.existsByNameAndCourse(
                        chapterName,
                        course
                )){


                    skippedCount++;

                    continue;

                }



                Chapter chapter = new Chapter();


                chapter.setCourse(course);

                chapter.setName(chapterName);



                chapterRepository.save(chapter);



                savedCount++;


            }



            return "Chapter Excel upload completed. Saved: "
                    + savedCount
                    + ", Skipped: "
                    + skippedCount;



        }
        catch(Exception e){

            logger.error(
                    "Chapter Excel upload failed",
                    e
            );


            throw new RuntimeException(
                    "Failed to upload Excel file"
            );

        }

    }
}