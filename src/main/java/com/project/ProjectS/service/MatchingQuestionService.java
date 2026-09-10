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
public class MatchingQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionMatchingPairRepository matchingPairRepository;

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
    // CREATE MATCHING QUESTION
    // ============================================================

    public MatchingQuestionResponseDTO createMatchingQuestion(
            MatchingQuestionRequestDTO request) {

        if (request.getPairs() == null || request.getPairs().isEmpty()) {
            throw new RuntimeException(
                    "At least one matching pair is required"
            );
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() ->
                        new RuntimeException("Chapter not found"));

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() ->
                        new RuntimeException("Topic not found"));

        QuestionType questionType =
                questionTypeRepository.findById(request.getQuestionTypeId())
                        .orElseThrow(() ->
                                new RuntimeException("Question type not found"));


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
        // CREATE MATCHING PAIRS
        // ============================================================

        List<MatchingPairResponseDTO> pairResponses =
                new ArrayList<>();

        for (MatchingPairRequestDTO pairRequest :
                request.getPairs()) {

            QuestionMatchingPair pair =
                    new QuestionMatchingPair();

            pair.setQuestion(savedQuestion);
            pair.setColumnA(pairRequest.getColumnA());
            pair.setColumnB(pairRequest.getColumnB());
            pair.setDisplayOrder(pairRequest.getDisplayOrder());

            QuestionMatchingPair savedPair =
                    matchingPairRepository.save(pair);


            MatchingPairResponseDTO pairResponse =
                    new MatchingPairResponseDTO();

            pairResponse.setPairId(
                    savedPair.getPairId()
            );

            pairResponse.setColumnA(
                    savedPair.getColumnA()
            );

            pairResponse.setColumnB(
                    savedPair.getColumnB()
            );

            pairResponse.setDisplayOrder(
                    savedPair.getDisplayOrder()
            );

            pairResponses.add(pairResponse);
        }


        return buildResponse(
                savedQuestion,
                pairResponses
        );
    }


    // ============================================================
    // GET MATCHING QUESTION
    // ============================================================

    public MatchingQuestionResponseDTO getMatchingQuestionById(
            Long questionId) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                ));


        List<QuestionMatchingPair> pairs =
                matchingPairRepository
                        .findByQuestion_QuestionIdOrderByDisplayOrderAsc(
                                questionId
                        );


        List<MatchingPairResponseDTO> pairResponses =
                new ArrayList<>();


        for (QuestionMatchingPair pair : pairs) {

            MatchingPairResponseDTO response =
                    new MatchingPairResponseDTO();

            response.setPairId(
                    pair.getPairId()
            );

            response.setColumnA(
                    pair.getColumnA()
            );

            response.setColumnB(
                    pair.getColumnB()
            );

            response.setDisplayOrder(
                    pair.getDisplayOrder()
            );

            pairResponses.add(response);
        }


        return buildResponse(
                question,
                pairResponses
        );
    }


    // ============================================================
    // GET ALL MATCHING QUESTIONS
    // ============================================================

    public List<MatchingQuestionResponseDTO>
    getAllMatchingQuestions() {

        List<Question> questions =
                questionRepository.findAll();

        List<MatchingQuestionResponseDTO> responses =
                new ArrayList<>();


        for (Question question : questions) {

            if (question.getQuestionType() == null) {
                continue;
            }

            if (!"MATCHING".equalsIgnoreCase(
                    question.getQuestionType().getQuestionType())) {

                continue;
            }


            List<QuestionMatchingPair> pairs =
                    matchingPairRepository
                            .findByQuestion_QuestionIdOrderByDisplayOrderAsc(
                                    question.getQuestionId()
                            );


            List<MatchingPairResponseDTO> pairResponses =
                    new ArrayList<>();


            for (QuestionMatchingPair pair : pairs) {

                MatchingPairResponseDTO response =
                        new MatchingPairResponseDTO();

                response.setPairId(
                        pair.getPairId()
                );

                response.setColumnA(
                        pair.getColumnA()
                );

                response.setColumnB(
                        pair.getColumnB()
                );

                response.setDisplayOrder(
                        pair.getDisplayOrder()
                );

                pairResponses.add(response);
            }


            responses.add(
                    buildResponse(
                            question,
                            pairResponses
                    )
            );
        }

        return responses;
    }


    // ============================================================
    // UPDATE MATCHING QUESTION
    // ============================================================

    public MatchingQuestionResponseDTO updateMatchingQuestion(
            Long questionId,
            MatchingQuestionRequestDTO request) {

        if (request.getPairs() == null ||
                request.getPairs().isEmpty()) {

            throw new RuntimeException(
                    "At least one matching pair is required"
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
        // DELETE OLD MATCHING PAIRS
        // ============================================================

        matchingPairRepository
                .deleteByQuestion_QuestionId(questionId);


        // ============================================================
        // CREATE NEW MATCHING PAIRS
        // ============================================================

        List<MatchingPairResponseDTO> pairResponses =
                new ArrayList<>();


        for (MatchingPairRequestDTO pairRequest :
                request.getPairs()) {

            QuestionMatchingPair pair =
                    new QuestionMatchingPair();

            pair.setQuestion(updatedQuestion);
            pair.setColumnA(pairRequest.getColumnA());
            pair.setColumnB(pairRequest.getColumnB());
            pair.setDisplayOrder(pairRequest.getDisplayOrder());

            QuestionMatchingPair savedPair =
                    matchingPairRepository.save(pair);


            MatchingPairResponseDTO response =
                    new MatchingPairResponseDTO();

            response.setPairId(
                    savedPair.getPairId()
            );

            response.setColumnA(
                    savedPair.getColumnA()
            );

            response.setColumnB(
                    savedPair.getColumnB()
            );

            response.setDisplayOrder(
                    savedPair.getDisplayOrder()
            );

            pairResponses.add(response);
        }


        return buildResponse(
                updatedQuestion,
                pairResponses
        );
    }


    // ============================================================
    // DELETE MATCHING QUESTION
    // ============================================================

    public String deleteMatchingQuestion(
            Long questionId) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                ));


        matchingPairRepository
                .deleteByQuestion_QuestionId(questionId);


        // Soft delete parent question
        question.setActiveRow(false);

        questionRepository.save(question);


        return "Matching question deleted successfully";
    }


    // ============================================================
    // BUILD RESPONSE
    // ============================================================

    private MatchingQuestionResponseDTO buildResponse(
            Question question,
            List<MatchingPairResponseDTO> pairs) {

        MatchingQuestionResponseDTO response =
                new MatchingQuestionResponseDTO();


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


        response.setPairs(pairs);


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