package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionCategory;

import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.QuestionCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class QuestionExcelMapper implements ExcelRowMapper<Question> {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;


    // =========================================================
    // MAP EXCEL ROW TO QUESTION
    // =========================================================

    @Override
    public Question map(Map<String, String> row) {

        // =====================================================
        // READ EXCEL VALUES
        // =====================================================

        String branchName =
                row.get("branch_name");

        String courseName =
                row.get("course_name");

        String chapterName =
                row.get("chapter_name");

        String categoryName =
                row.get("category_name");

        String questionText =
                row.get("question_text");


        // =====================================================
        // VALIDATION
        // =====================================================

        if (isBlank(branchName)) {

            throw new RuntimeException(
                    "Branch name is required"
            );
        }

        if (isBlank(courseName)) {

            throw new RuntimeException(
                    "Course name is required"
            );
        }

        if (isBlank(chapterName)) {

            throw new RuntimeException(
                    "Chapter name is required"
            );
        }

        if (isBlank(categoryName)) {

            throw new RuntimeException(
                    "Category name is required"
            );
        }

        if (isBlank(questionText)) {

            throw new RuntimeException(
                    "Question text is required"
            );
        }


        // =====================================================
        // FIND BRANCH
        // =====================================================

        List<Branch> branches =
                branchRepository.findByBranchName(
                        branchName.trim()
                );

        if (branches.isEmpty()) {

            throw new RuntimeException(
                    "Branch not found: "
                            + branchName
            );
        }

        if (branches.size() > 1) {

            throw new RuntimeException(
                    "Multiple branches found with name: "
                            + branchName
            );
        }

        Branch branch =
                branches.get(0);


        // =====================================================
        // FIND COURSE
        // =====================================================

        Course course =
                courseRepository
                        .findByNameAndBranch(
                                courseName.trim(),
                                branch
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found: "
                                                + courseName
                                                + " for branch: "
                                                + branchName
                                )
                        );


        // =====================================================
        // FIND CHAPTER
        // =====================================================

        Chapter chapter =
                chapterRepository
                        .findByNameAndCourse(
                                chapterName.trim(),
                                course
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chapter not found: "
                                                + chapterName
                                                + " for course: "
                                                + courseName
                                )
                        );


        // =====================================================
        // FIND QUESTION CATEGORY
        // =====================================================

        QuestionCategory category =
                questionCategoryRepository
                        .findByCourseAndChapterAndName(
                                course,
                                chapter,
                                categoryName.trim()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question category not found: "
                                                + categoryName
                                                + " for chapter: "
                                                + chapterName
                                )
                        );


        // =====================================================
        // CREATE QUESTION
        // =====================================================

        Question question =
                new Question();

        question.setCourse(course);

        question.setChapter(chapter);

        question.setQuestionCategory(category);

        question.setQuestionText(
                questionText.trim()
        );

        question.setActiveRow(true);


        return question;
    }


    // =========================================================
    // HELPER
    // =========================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}