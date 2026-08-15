package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.QuestionCategory;
import com.project.ProjectS.mapper.QuestionCategoryExcelMapper;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CollegeRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.QuestionCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Component
public class QuestionCategoryExcelProcessor {
    @Autowired
    public QuestionCategoryExcelProcessor(QuestionCategoryExcelMapper questionCategoryExcelMapper, QuestionCategoryRepository questionCategoryRepository, CollegeRepository collegeRepository, BranchRepository branchRepository, CourseRepository courseRepository, ChapterRepository chapterRepository) {
        this.questionCategoryExcelMapper = questionCategoryExcelMapper;
        this.questionCategoryRepository = questionCategoryRepository;
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
    }


    private static final Logger logger = LogManager.getLogger(QuestionCategoryExcelProcessor.class);
    private final QuestionCategoryExcelMapper questionCategoryExcelMapper;
    private final QuestionCategoryRepository questionCategoryRepository;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;


    @Transactional
    public void process(List<Map<String, String>> excelData) {

        int savedCount = 0;
        int skippedCount = 0;

        for (Map<String, String> row : excelData) {

            // Skip empty rows
            if (row.values()
                    .stream()
                    .allMatch(value ->
                            value == null || value.isBlank())) {

                continue;
            }

            try {

                // -----------------------------------------
                // Read Excel values
                // -----------------------------------------

                String collegeName =
                        row.get("college");

                String branchName =
                        row.get("branch");

                String courseName =
                        row.get("course");

                String chapterName =
                        row.get("chapter");

                String categoryName =
                        row.get("name");


                if (collegeName == null ||
                        collegeName.isBlank() ||

                        branchName == null ||
                        branchName.isBlank() ||

                        courseName == null ||
                        courseName.isBlank() ||

                        chapterName == null ||
                        chapterName.isBlank() ||

                        categoryName == null ||
                        categoryName.isBlank()) {

                    skippedCount++;
                    continue;
                }


                // -----------------------------------------
                // Find College
                // -----------------------------------------

                List<College> colleges =
                        collegeRepository
                                .findByInstituteName(
                                        collegeName.trim()
                                );

                if (colleges.isEmpty()) {

                    logger.warn("College not found: {}", collegeName);

                    skippedCount++;
                    continue;
                }

                if (colleges.size() > 1) {

                    logger.warn("Multiple colleges found: {}", collegeName);

                    skippedCount++;
                    continue;
                }

                College college =
                        colleges.get(0);


                // -----------------------------------------
                // Find Branch
                // -----------------------------------------

                List<Branch> branches =
                        branchRepository
                                .findByBranchName(
                                        branchName.trim()
                                );

                Branch branch = null;

                for (Branch b : branches) {

                    if (b.getCollege() != null &&
                            b.getCollege()
                                    .getCollegeId()
                                    .equals(
                                            college.getCollegeId()
                                    )) {

                        branch = b;
                        break;
                    }
                }

                if (branch == null) {

                    logger.warn("Branch not found: {} for College: {}", branchName, collegeName);

                    skippedCount++;
                    continue;
                }


                // -----------------------------------------
                // Find Course
                // -----------------------------------------

                Course course =
                        courseRepository
                                .findByNameAndBranch(
                                        courseName.trim(),
                                        branch
                                )
                                .orElse(null);

                if (course == null) {

                    logger.warn("Course not found: {}", courseName);

                    skippedCount++;
                    continue;
                }


                // -----------------------------------------
                // Find Chapter
                // -----------------------------------------

                Chapter chapter =
                        chapterRepository
                                .findByNameAndCourse(
                                        chapterName.trim(),
                                        course
                                )
                                .orElse(null);

                if (chapter == null) {

                    logger.warn("Chapter not found: {}", chapterName);

                    skippedCount++;
                    continue;
                }


                // -----------------------------------------
                // Duplicate Check
                // -----------------------------------------

                boolean exists =
                        questionCategoryRepository
                                .existsByCourseAndChapterAndName(
                                        course,
                                        chapter,
                                        categoryName.trim()
                                );

                if (exists) {

                    logger.info("Question Category already exists: {}", categoryName);

                    skippedCount++;
                    continue;
                }


                // -----------------------------------------
                // Map basic fields
                // -----------------------------------------

                QuestionCategory category =
                        questionCategoryExcelMapper
                                .map(row);


                if (category == null) {

                    skippedCount++;
                    continue;
                }


                // -----------------------------------------
                // Set mandatory relationships
                // -----------------------------------------

                category.setCourse(course);
                category.setChapter(chapter);


                // -----------------------------------------
                // Save
                // -----------------------------------------

                questionCategoryRepository
                        .save(category);

                savedCount++;

            } catch (Exception e) {

                skippedCount++;

                logger.error("Failed to process Question Category row: {}", e.getMessage(), e);
            }
        }

        logger.info("Question Category upload completed. Saved: {}, Skipped: {}", savedCount, skippedCount);
    }
}
