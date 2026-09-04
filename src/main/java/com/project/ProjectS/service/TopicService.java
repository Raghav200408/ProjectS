package com.project.ProjectS.service;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Topic;
import com.project.ProjectS.entity.Subject;
import com.project.ProjectS.model.TopicRequestDTO;
import com.project.ProjectS.model.TopicResponseDTO;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.TopicRepository;
import com.project.ProjectS.repository.SubjectRepository;
import com.project.ProjectS.processor.TopicExcelProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class TopicService {
    @Autowired
    public TopicService(TopicRepository repository, CourseRepository courseRepository, ChapterRepository chapterRepository, SubjectRepository subjectRepository, TopicExcelProcessor topicExcelProcessor) {
        this.repository = repository;
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.subjectRepository = subjectRepository;
        this.topicExcelProcessor = topicExcelProcessor;
    }

    private final TopicRepository repository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final TopicExcelProcessor topicExcelProcessor;

    private static final Logger logger =
            LogManager.getLogger(TopicService.class);


    // =========================================================
    // CREATE
    // =========================================================

    public String create(TopicRequestDTO request) {

        logger.info(
                "Creating topic with name: {}",
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

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        validateHierarchy(course, subject, chapter);


        // Duplicate check based on
        // Course + Chapter + Topic Name

        if (repository.existsByCourseAndSubjectAndChapterAndName(
                course,
                subject,
                chapter,
                request.getName()
        )) {

            logger.warn(
                    "Topic already exists: {}",
                    request.getName()
            );

            throw new RuntimeException(
                    "Topic already exists"
            );
        }


        Topic topic =
                new Topic();

        topic.setCourse(course);
        topic.setSubject(subject);
        topic.setChapter(chapter);
        topic.setName(request.getName());
        topic.setActiveRow(request.getActiveRow());


        repository.save(topic);


        logger.info(
                "Topic created successfully with name: {}",
                request.getName()
        );

        return "Topic created successfully";
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<TopicResponseDTO> getAll() {

        List<Topic> categories =
                repository.findAll();

        List<TopicResponseDTO> response =
                new ArrayList<>();


        for (Topic topic : categories) {

            TopicResponseDTO dto =
                    new TopicResponseDTO();


            dto.setTopicId(
                    topic.getTopicId()
            );


            if (topic.getCourse() != null) {

                dto.setCourseId(
                        topic.getCourse().getCourseId()
                );

                dto.setCourseName(
                        topic.getCourse().getName()
                );
            }

            setSubject(dto, topic);


            if (topic.getChapter() != null) {

                dto.setChapterId(
                        topic.getChapter().getChapterId()
                );

                dto.setChapterName(
                        topic.getChapter().getName()
                );
            }


            dto.setName(
                    topic.getName()
            );

            dto.setActiveRow(
                    topic.getActiveRow()
            );

            dto.setRowStatus(
                    topic.getRowStatus()
            );

            dto.setOrderOf(
                    topic.getOrderOf()
            );

            dto.setCreatedAt(
                    topic.getCreatedAt()
            );

            dto.setUpdatedAt(
                    topic.getUpdatedAt()
            );


            response.add(dto);
        }

        return response;
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public TopicResponseDTO getById(Long id) {

        Topic topic =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Topic not found"
                                )
                        );


        TopicResponseDTO dto =
                new TopicResponseDTO();


        dto.setTopicId(
                topic.getTopicId()
        );


        if (topic.getCourse() != null) {

            dto.setCourseId(
                    topic.getCourse().getCourseId()
            );

            dto.setCourseName(
                    topic.getCourse().getName()
            );
        }

        setSubject(dto, topic);


        if (topic.getChapter() != null) {

            dto.setChapterId(
                    topic.getChapter().getChapterId()
            );

            dto.setChapterName(
                    topic.getChapter().getName()
            );
        }


        dto.setName(
                topic.getName()
        );

        dto.setActiveRow(
                topic.getActiveRow()
        );

        dto.setRowStatus(
                topic.getRowStatus()
        );

        dto.setOrderOf(
                topic.getOrderOf()
        );

        dto.setCreatedAt(
                topic.getCreatedAt()
        );

        dto.setUpdatedAt(
                topic.getUpdatedAt()
        );


        return dto;
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public String update(
            Long id,
            TopicRequestDTO request) {

        logger.info(
                "Updating Topic with ID: {}",
                id
        );


        Topic topic =
                repository.findById(id)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Topic not found with ID: {}",
                                    id
                            );

                            return new RuntimeException(
                                    "Topic not found"
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

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        validateHierarchy(course, subject, chapter);


        // Check duplicate only when another topic
        // with the same Course + Chapter + Name exists

        Topic existing =
                repository.findByCourseAndSubjectAndChapterAndName(
                        course,
                        subject,
                        chapter,
                        request.getName()
                ).orElse(null);


        if (existing != null &&
                !existing.getTopicId().equals(id)) {

            throw new RuntimeException(
                    "Topic already exists"
            );
        }


        topic.setCourse(course);
        topic.setSubject(subject);
        topic.setChapter(chapter);
        topic.setName(request.getName());
        topic.setActiveRow(request.getActiveRow());


        repository.save(topic);


        logger.info(
                "Topic updated successfully with ID: {}",
                id
        );

        return "Topic updated successfully";
    }


    // =========================================================
    // DELETE
    // =========================================================

    public String delete(Long id) {

        Topic topic =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Topic not found"
                                )
                        );


        repository.delete(topic);


        return "Topic deleted successfully";
    }


    // =========================================================
    // EXCEL UPLOAD
    // =========================================================

    public String uploadTopic(
            MultipartFile file) {

        logger.info(
                "Starting Topic Excel upload process."
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
                    "Topic Excel file received: {}",
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
             * TopicExcelProcessor
             *
             * Therefore the old XSSFWorkbook code should
             * NOT be kept here.
             */


            return "Topic Excel upload request received successfully";

        } catch (Exception e) {

            logger.error(
                    "Failed while uploading Topic Excel",
                    e
            );

            throw new RuntimeException(
                    "Failed to upload Excel file: "
                            + e.getMessage()
            );
        }
    }

    private void validateHierarchy(Course course, Subject subject, Chapter chapter) {
        if (!subject.getCourse().getCourseId().equals(course.getCourseId())
                || !chapter.getCourse().getCourseId().equals(course.getCourseId())
                || !chapter.getSubject().getSubjectId().equals(subject.getSubjectId())) {
            throw new RuntimeException("Course, subject, and chapter must belong to the same hierarchy");
        }
    }

    private void setSubject(TopicResponseDTO dto, Topic topic) {
        dto.setSubjectId(topic.getSubject().getSubjectId());
        dto.setSubjectName(topic.getSubject().getSubjectName());
    }
}
