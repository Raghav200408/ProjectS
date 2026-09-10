package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FillInTheBlankQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionFillBlankAnswerRepository fillBlankAnswerRepository;

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


    // ============================================================
    // CREATE FILL IN THE BLANK QUESTION
    // ============================================================

    public FillInTheBlankQuestionResponseDTO createFillInTheBlankQuestion(
            FillInTheBlankQuestionRequestDTO request) {

        if (request.getAnswers() == null ||
                request.getAnswers().isEmpty()) {

            throw new RuntimeException(
                    "At least one answer is required"
            );
        }


        Course course =
                courseRepository.findById(request.getCourseId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));


        Subject subject =
                subjectRepository.findById(request.getSubjectId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subject not found"
                                ));


        Chapter chapter =
                chapterRepository.findById(request.getChapterId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chapter not found"
                                ));


        Topic topic =
                topicRepository.findById(request.getTopicId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Topic not found"
                                ));


        QuestionType questionType =
                questionTypeRepository
                        .findById(request.getQuestionTypeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question type not found"
                                ));


        // ============================================================
        // VALIDATE HIERARCHY
        // ============================================================

        validateHierarchy(
                course,
                subject,
                chapter,
                topic
        );


        // ============================================================
        // CREATE PARENT QUESTION
        // ============================================================

        Question question = new Question();

        question.setCourse(course);
        question.setSubject(subject);
        question.setChapter(chapter);
        question.setTopic(topic);
        question.setQuestionType(questionType);
        question.setQuestionText(request.getQuestionText());
        question.setActiveRow(true);

        Question savedQuestion =
                questionRepository.save(question);


        // ============================================================
        // CREATE ANSWERS
        // ============================================================

        List<FillInTheBlankAnswerResponseDTO> answerResponses =
                new ArrayList<>();


        for (FillInTheBlankAnswerRequestDTO answerRequest :
                request.getAnswers()) {

            QuestionFillBlankAnswer answer =
                    new QuestionFillBlankAnswer();

            answer.setQuestion(savedQuestion);
            answer.setAnswerText(answerRequest.getAnswerText());
            answer.setDisplayOrder(answerRequest.getDisplayOrder());


            QuestionFillBlankAnswer savedAnswer =
                    fillBlankAnswerRepository.save(answer);


            FillInTheBlankAnswerResponseDTO answerResponse =
                    new FillInTheBlankAnswerResponseDTO();

            answerResponse.setAnswerId(
                    savedAnswer.getAnswerId()
            );

            answerResponse.setAnswerText(
                    savedAnswer.getAnswerText()
            );

            answerResponse.setDisplayOrder(
                    savedAnswer.getDisplayOrder()
            );

            answerResponses.add(answerResponse);
        }


        return buildResponse(
                savedQuestion,
                answerResponses
        );
    }


    // ============================================================
    // GET FILL IN THE BLANK QUESTION
    // ============================================================

    public FillInTheBlankQuestionResponseDTO getFillInTheBlankQuestionById(
            Long questionId) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                ));


        List<QuestionFillBlankAnswer> answers =
                fillBlankAnswerRepository
                        .findByQuestionQuestionIdOrderByDisplayOrderAsc(
                                questionId
                        );


        List<FillInTheBlankAnswerResponseDTO> answerResponses =
                new ArrayList<>();


        for (QuestionFillBlankAnswer answer : answers) {

            FillInTheBlankAnswerResponseDTO response =
                    new FillInTheBlankAnswerResponseDTO();

            response.setAnswerId(
                    answer.getAnswerId()
            );

            response.setAnswerText(
                    answer.getAnswerText()
            );

            response.setDisplayOrder(
                    answer.getDisplayOrder()
            );

            answerResponses.add(response);
        }


        return buildResponse(
                question,
                answerResponses
        );
    }


    // ============================================================
    // GET ALL FILL IN THE BLANK QUESTIONS
    // ============================================================

    public List<FillInTheBlankQuestionResponseDTO>
    getAllFillInTheBlankQuestions() {

        List<Question> questions =
                questionRepository.findAll();

        List<FillInTheBlankQuestionResponseDTO> responses =
                new ArrayList<>();


        for (Question question : questions) {

            if (question.getQuestionType() == null) {
                continue;
            }


            if (!"FILL_IN_THE_BLANK".equalsIgnoreCase(
                    question.getQuestionType().getQuestionType())) {

                continue;
            }


            List<QuestionFillBlankAnswer> answers =
                    fillBlankAnswerRepository
                            .findByQuestionQuestionIdOrderByDisplayOrderAsc(
                                    question.getQuestionId()
                            );


            List<FillInTheBlankAnswerResponseDTO> answerResponses =
                    new ArrayList<>();


            for (QuestionFillBlankAnswer answer : answers) {

                FillInTheBlankAnswerResponseDTO response =
                        new FillInTheBlankAnswerResponseDTO();

                response.setAnswerId(
                        answer.getAnswerId()
                );

                response.setAnswerText(
                        answer.getAnswerText()
                );

                response.setDisplayOrder(
                        answer.getDisplayOrder()
                );

                answerResponses.add(response);
            }


            responses.add(
                    buildResponse(
                            question,
                            answerResponses
                    )
            );
        }


        return responses;
    }


    // ============================================================
    // UPDATE FILL IN THE BLANK QUESTION
    // ============================================================

    public FillInTheBlankQuestionResponseDTO updateFillInTheBlankQuestion(
            Long questionId,
            FillInTheBlankQuestionRequestDTO request) {

        if (request.getAnswers() == null ||
                request.getAnswers().isEmpty()) {

            throw new RuntimeException(
                    "At least one answer is required"
            );
        }


        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                ));


        Course course =
                courseRepository.findById(request.getCourseId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));


        Subject subject =
                subjectRepository.findById(request.getSubjectId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subject not found"
                                ));


        Chapter chapter =
                chapterRepository.findById(request.getChapterId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Chapter not found"
                                ));


        Topic topic =
                topicRepository.findById(request.getTopicId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Topic not found"
                                ));


        QuestionType questionType =
                questionTypeRepository
                        .findById(request.getQuestionTypeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question type not found"
                                ));


        // ============================================================
        // VALIDATE HIERARCHY
        // ============================================================

        validateHierarchy(
                course,
                subject,
                chapter,
                topic
        );


        // ============================================================
        // UPDATE PARENT QUESTION
        // ============================================================

        question.setCourse(course);
        question.setSubject(subject);
        question.setChapter(chapter);
        question.setTopic(topic);
        question.setQuestionType(questionType);
        question.setQuestionText(request.getQuestionText());


        Question updatedQuestion =
                questionRepository.save(question);


        // ============================================================
        // DELETE OLD ANSWERS
        // ============================================================

        fillBlankAnswerRepository
                .deleteByQuestionQuestionId(questionId);


        // ============================================================
        // CREATE NEW ANSWERS
        // ============================================================

        List<FillInTheBlankAnswerResponseDTO> answerResponses =
                new ArrayList<>();


        for (FillInTheBlankAnswerRequestDTO answerRequest :
                request.getAnswers()) {

            QuestionFillBlankAnswer answer =
                    new QuestionFillBlankAnswer();

            answer.setQuestion(updatedQuestion);
            answer.setAnswerText(answerRequest.getAnswerText());
            answer.setDisplayOrder(answerRequest.getDisplayOrder());


            QuestionFillBlankAnswer savedAnswer =
                    fillBlankAnswerRepository.save(answer);


            FillInTheBlankAnswerResponseDTO response =
                    new FillInTheBlankAnswerResponseDTO();

            response.setAnswerId(
                    savedAnswer.getAnswerId()
            );

            response.setAnswerText(
                    savedAnswer.getAnswerText()
            );

            response.setDisplayOrder(
                    savedAnswer.getDisplayOrder()
            );

            answerResponses.add(response);
        }


        return buildResponse(
                updatedQuestion,
                answerResponses
        );
    }


    // ============================================================
    // DELETE FILL IN THE BLANK QUESTION
    // ============================================================

    public String deleteFillInTheBlankQuestion(
            Long questionId) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                ));


        // Delete child answers first
        fillBlankAnswerRepository
                .deleteByQuestionQuestionId(questionId);


        // Soft delete parent question
        question.setActiveRow(false);

        questionRepository.save(question);


        return "Fill in the blank question deleted successfully";
    }


    // ============================================================
    // BUILD RESPONSE
    // ============================================================

    private FillInTheBlankQuestionResponseDTO buildResponse(
            Question question,
            List<FillInTheBlankAnswerResponseDTO> answers) {

        FillInTheBlankQuestionResponseDTO response =
                new FillInTheBlankQuestionResponseDTO();


        response.setQuestionId(
                question.getQuestionId()
        );


        if (question.getCourse() != null) {

            response.setCourseId(
                    question.getCourse().getCourseId()
            );
        }


        if (question.getSubject() != null) {

            response.setSubjectId(
                    question.getSubject().getSubjectId()
            );
        }


        if (question.getChapter() != null) {

            response.setChapterId(
                    question.getChapter().getChapterId()
            );
        }


        if (question.getTopic() != null) {

            response.setTopicId(
                    question.getTopic().getTopicId()
            );
        }


        if (question.getQuestionType() != null) {

            response.setQuestionTypeId(
                    question.getQuestionType()
                            .getQuestionTypeId()
            );
        }


        response.setQuestionText(
                question.getQuestionText()
        );


        response.setAnswers(answers);


        return response;
    }


    // ============================================================
    // VALIDATE COURSE → SUBJECT → CHAPTER → TOPIC
    // ============================================================

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