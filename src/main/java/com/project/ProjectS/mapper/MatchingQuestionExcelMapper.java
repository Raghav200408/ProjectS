package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MatchingQuestionExcelMapper {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;


    public Question mapQuestion(
            Map<String, String> row,
            Integer courseId,
            Integer chapterId,
            Integer topicId) {

        String questionText = getValue(row, "question_text");

        if (questionText == null || questionText.isBlank()) {
            throw new RuntimeException(
                    "question_text is required"
            );
        }


        String questionTypeValue =
                getValue(row, "question_type");

        if (questionTypeValue == null ||
                questionTypeValue.isBlank()) {

            throw new RuntimeException(
                    "question_type is required"
            );
        }


        String normalizedQuestionType =
                questionTypeValue
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_");


        if (!"MATCH_THE_FOLLOWING".equals(
                normalizedQuestionType)) {

            throw new RuntimeException(
                    "Invalid question_type. Expected MATCH_THE_FOLLOWING"
            );
        }


        Course course =
                courseRepository.findById(
                        Long.valueOf(courseId)
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Course not found: " + courseId
                        )
                );


        Chapter chapter =
                chapterRepository.findById(
                        Long.valueOf(chapterId)
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Chapter not found: " + chapterId
                        )
                );


        Topic topic =
                topicRepository.findById(
                        Long.valueOf(topicId)
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Topic not found: " + topicId
                        )
                );


        if (topic.getSubject() == null) {
            throw new RuntimeException(
                    "Topic does not have a subject"
            );
        }


        Subject subject =
                subjectRepository.findById(
                        topic.getSubject().getSubjectId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Subject not found"
                        )
                );


        QuestionType questionType =
                questionTypeRepository
                        .findByQuestionType(
                                "MATCH_THE_FOLLOWING"
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "MATCH_THE_FOLLOWING question type not found"
                                )
                        );


        validateHierarchy(
                course,
                subject,
                chapter,
                topic
        );


        Question question = new Question();

        question.setCourse(course);
        question.setSubject(subject);
        question.setChapter(chapter);
        question.setTopic(topic);
        question.setQuestionType(questionType);
        question.setQuestionText(questionText.trim());
        question.setActiveRow(true);

        return question;
    }


    private String getValue(
            Map<String, String> row,
            String key) {

        String value = row.get(key);

        if (value == null) {
            value = row.get(key.toLowerCase());
        }

        return value == null ? null : value.trim();
    }


    private void validateHierarchy(
            Course course,
            Subject subject,
            Chapter chapter,
            Topic topic) {

        if (subject.getCourse() == null ||
                !subject.getCourse()
                        .getCourseId()
                        .equals(course.getCourseId())) {

            throw new RuntimeException(
                    "Subject does not belong to the selected course"
            );
        }


        if (chapter.getCourse() == null ||
                !chapter.getCourse()
                        .getCourseId()
                        .equals(course.getCourseId())) {

            throw new RuntimeException(
                    "Chapter does not belong to the selected course"
            );
        }


        if (chapter.getSubject() == null ||
                !chapter.getSubject()
                        .getSubjectId()
                        .equals(subject.getSubjectId())) {

            throw new RuntimeException(
                    "Chapter does not belong to the selected subject"
            );
        }


        if (topic.getCourse() == null ||
                !topic.getCourse()
                        .getCourseId()
                        .equals(course.getCourseId())) {

            throw new RuntimeException(
                    "Topic does not belong to the selected course"
            );
        }


        if (topic.getSubject() == null ||
                !topic.getSubject()
                        .getSubjectId()
                        .equals(subject.getSubjectId())) {

            throw new RuntimeException(
                    "Topic does not belong to the selected subject"
            );
        }


        if (topic.getChapter() == null ||
                !topic.getChapter()
                        .getChapterId()
                        .equals(chapter.getChapterId())) {

            throw new RuntimeException(
                    "Topic does not belong to the selected chapter"
            );
        }
    }
}
