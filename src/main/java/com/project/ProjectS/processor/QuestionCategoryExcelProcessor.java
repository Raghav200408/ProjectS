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

@Component
public class QuestionCategoryExcelProcessor {

    @Autowired
    private QuestionCategoryExcelMapper questionCategoryExcelMapper;

    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;


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

                    System.out.println(
                            "College not found: "
                                    + collegeName
                    );

                    skippedCount++;
                    continue;
                }

                if (colleges.size() > 1) {

                    System.out.println(
                            "Multiple colleges found: "
                                    + collegeName
                    );

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

                    System.out.println(
                            "Branch not found: "
                                    + branchName
                                    + " for College: "
                                    + collegeName
                    );

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

                    System.out.println(
                            "Course not found: "
                                    + courseName
                    );

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

                    System.out.println(
                            "Chapter not found: "
                                    + chapterName
                    );

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

                    System.out.println(
                            "Question Category already exists: "
                                    + categoryName
                    );

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

                System.out.println(
                        "Failed to process Question Category row: "
                                + e.getMessage()
                );
            }
        }

        System.out.println(
                "Question Category upload completed. "
                        + "Saved: "
                        + savedCount
                        + ", Skipped: "
                        + skippedCount
        );
    }
}