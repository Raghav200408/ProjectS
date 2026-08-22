package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.QuestionCategory;

import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.QuestionCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QuestionExcelMapper {

    private final CourseRepository courseRepository;

    private final ChapterRepository chapterRepository;

    private final QuestionCategoryRepository questionCategoryRepository;

    @Autowired
    public QuestionExcelMapper(
            CourseRepository courseRepository,
            ChapterRepository chapterRepository,
            QuestionCategoryRepository questionCategoryRepository) {

        this.courseRepository =
                courseRepository;

        this.chapterRepository =
                chapterRepository;

        this.questionCategoryRepository =
                questionCategoryRepository;
    }

    // =========================================================
    // MAP EXCEL ROW TO QUESTION
    // =========================================================

    public Question map(
            Map<String, String> row,
            Integer courseId,
            Integer chapterId,
            Integer categoryId) {

        String questionText =
                row.get("question_text");

        if (isBlank(questionText)) {

            throw new RuntimeException(
                    "Question text is required"
            );
        }

        if (courseId == null) {

            throw new RuntimeException(
                    "Course ID is required"
            );
        }

        if (chapterId == null) {

            throw new RuntimeException(
                    "Chapter ID is required"
            );
        }

        if (categoryId == null) {

            throw new RuntimeException(
                    "Category ID is required"
            );
        }

        // =====================================================
        // FIND COURSE
        // =====================================================

        Course course =
                courseRepository
                        .findById(
                                courseId.longValue()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found with id: "
                                                + courseId
                                )
                        );

        // =====================================================
        // FIND CHAPTER
        // =====================================================

        Chapter chapter =
                chapterRepository
                        .findById(
                                chapterId.longValue()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chapter not found with id: "
                                                + chapterId
                                )
                        );

        // =====================================================
        // VALIDATE CHAPTER -> COURSE
        // =====================================================

        if (chapter.getCourse() == null ||
                !chapter.getCourse()
                        .getCourseId()
                        .equals(course.getCourseId())) {

            throw new RuntimeException(
                    "Chapter " + chapterId +
                            " does not belong to course " +
                            courseId
            );
        }

        // =====================================================
        // FIND CATEGORY
        // =====================================================

        QuestionCategory category =
                questionCategoryRepository
                        .findById(
                                categoryId.longValue()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question category not found with id: "
                                                + categoryId
                                )
                        );

        // =====================================================
        // VALIDATE CATEGORY -> COURSE
        // =====================================================

        if (category.getCourse() == null ||
                !category.getCourse()
                        .getCourseId()
                        .equals(course.getCourseId())) {

            throw new RuntimeException(
                    "Question category " + categoryId +
                            " does not belong to course " +
                            courseId
            );
        }

        // =====================================================
        // VALIDATE CATEGORY -> CHAPTER
        // =====================================================

        if (category.getChapter() == null ||
                !category.getChapter()
                        .getChapterId()
                        .equals(chapter.getChapterId())) {

            throw new RuntimeException(
                    "Question category " + categoryId +
                            " does not belong to chapter " +
                            chapterId
            );
        }

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
    // IS BLANK
    // =========================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}