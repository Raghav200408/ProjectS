package com.project.ProjectS.service;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.McqOption;
import com.project.ProjectS.entity.McqQuestion;
import com.project.ProjectS.entity.Question;
import com.project.ProjectS.entity.Topic;
import com.project.ProjectS.model.McqOptionDTO;
import com.project.ProjectS.model.McqQuestionRequestDTO;
import com.project.ProjectS.model.McqQuestionResponseDTO;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.McqOptionRepository;
import com.project.ProjectS.repository.McqQuestionRepository;
import com.project.ProjectS.repository.TopicRepository;
import com.project.ProjectS.repository.QuestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.project.ProjectS.entity.AnswerEvent;
import com.project.ProjectS.entity.QuestionAnswer;
import com.project.ProjectS.entity.User;

import com.project.ProjectS.model.McqAnswerSubmissionDTO;
import com.project.ProjectS.model.McqSubmissionRequestDTO;
import com.project.ProjectS.model.McqSubmissionResponseDTO;
import com.project.ProjectS.model.McqSubmissionResultDTO;

import com.project.ProjectS.repository.AnswerEventRepository;
import com.project.ProjectS.repository.QuestionAnswerRepository;
import com.project.ProjectS.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class McqQuestionService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final McqOptionRepository mcqOptionRepository;

    private final AnswerEventRepository answerEventRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final UserRepository userRepository;


    public McqQuestionService(
            QuestionRepository questionRepository,
            CourseRepository courseRepository,
            ChapterRepository chapterRepository,
            TopicRepository topicRepository,
            McqQuestionRepository mcqQuestionRepository,
            McqOptionRepository mcqOptionRepository,
            AnswerEventRepository answerEventRepository,
            QuestionAnswerRepository questionAnswerRepository,
            UserRepository userRepository
    ) {
        this.questionRepository = questionRepository;
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.topicRepository = topicRepository;
        this.mcqQuestionRepository = mcqQuestionRepository;
        this.mcqOptionRepository = mcqOptionRepository;
        this.answerEventRepository = answerEventRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.userRepository = userRepository;
    }


    // ==========================================
    // 1. CREATE MCQ
    // ==========================================

    @Transactional
    public McqQuestionResponseDTO createMcqQuestion(
            McqQuestionRequestDTO request
    ) {

        Course course = getCourse(request.getCourseId());
        Chapter chapter = getChapter(request.getChapterId());
        Topic topic =
                getTopic(request.getTopicId());


        // Create main question
        Question question = new Question();

        question.setCourse(course);
        question.setChapter(chapter);
        question.setTopic(topic);
        question.setSubject(topic.getSubject());
        question.setQuestionText(request.getQuestionText());

        Question savedQuestion =
                questionRepository.save(question);


        // Create MCQ configuration
        McqQuestion mcqQuestion = new McqQuestion();

        mcqQuestion.setQuestionId(
                savedQuestion.getQuestionId()
        );

        mcqQuestion.setQuestionType(
                request.getQuestionType()
        );

        mcqQuestion.setMarks(
                request.getMarks()
        );

        mcqQuestion.setActiveRow(true);

        mcqQuestionRepository.save(mcqQuestion);


        // Create options
        List<McqOption> options =
                request.getOptions()
                        .stream()
                        .map(optionDTO ->
                                createOption(
                                        savedQuestion.getQuestionId(),
                                        optionDTO
                                )
                        )
                        .collect(Collectors.toList());


        mcqOptionRepository.saveAll(options);

        return buildResponse(
                savedQuestion,
                mcqQuestion,
                options
        );
    }


    // ==========================================
    // 2. GET MCQ BY ID
    // ==========================================

    @Transactional(readOnly = true)
    public McqQuestionResponseDTO getMcqQuestionById(
            Long questionId
    ) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                )
                        );


        McqQuestion mcqQuestion =
                mcqQuestionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "MCQ question not found"
                                )
                        );


        if (!Boolean.TRUE.equals(
                mcqQuestion.getActiveRow()
        )) {
            throw new RuntimeException(
                    "MCQ question is inactive"
            );
        }


        List<McqOption> options =
                mcqOptionRepository
                        .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                questionId
                        );


        return buildResponse(
                question,
                mcqQuestion,
                options
        );
    }


    // ==========================================
    // 3. GET ALL MCQs
    // ==========================================

    @Transactional(readOnly = true)
    public List<McqQuestionResponseDTO> getAllMcqQuestions() {

        List<McqQuestion> mcqQuestions =
                mcqQuestionRepository.findAll()
                        .stream()
                        .filter(mcq ->
                                Boolean.TRUE.equals(
                                        mcq.getActiveRow()
                                )
                        )
                        .collect(Collectors.toList());


        return mcqQuestions.stream()
                .map(mcq -> {

                    Question question =
                            questionRepository
                                    .findById(
                                            mcq.getQuestionId()
                                    )
                                    .orElse(null);

                    if (question == null ||
                            !Boolean.TRUE.equals(
                                    question.getActiveRow()
                            )) {
                        return null;
                    }

                    List<McqOption> options =
                            mcqOptionRepository
                                    .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                            question.getQuestionId()
                                    );

                    return buildResponse(
                            question,
                            mcq,
                            options
                    );
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }


    // ==========================================
    // 4. FILTER MCQs
    // ==========================================

    @Transactional(readOnly = true)
    public List<McqQuestionResponseDTO>
    getMcqQuestionsByFilter(
            Long courseId,
            Long chapterId,
            Long topicId
    ) {

        List<Question> questions =
                questionRepository
                        .findByCourse_CourseIdAndChapter_ChapterIdAndTopic_TopicIdAndActiveRowTrue(
                                courseId,
                                chapterId,
                                topicId
                        );


        return questions.stream()
                .map(question -> {

                    McqQuestion mcqQuestion =
                            mcqQuestionRepository
                                    .findById(
                                            question.getQuestionId()
                                    )
                                    .orElse(null);

                    // Ignore non-MCQ questions
                    if (mcqQuestion == null ||
                            !Boolean.TRUE.equals(
                                    mcqQuestion.getActiveRow()
                            )) {
                        return null;
                    }

                    List<McqOption> options =
                            mcqOptionRepository
                                    .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                            question.getQuestionId()
                                    );

                    return buildResponse(
                            question,
                            mcqQuestion,
                            options
                    );
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }


    // ==========================================
    // 5. UPDATE MCQ
    // ==========================================

    @Transactional
    public McqQuestionResponseDTO updateMcqQuestion(
            Long questionId,
            McqQuestionRequestDTO request
    ) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                )
                        );


        McqQuestion mcqQuestion =
                mcqQuestionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "MCQ question not found"
                                )
                        );


        Course course = getCourse(request.getCourseId());
        Chapter chapter = getChapter(request.getChapterId());
        Topic topic =
                getTopic(request.getTopicId());


        // Update main question
        question.setCourse(course);
        question.setChapter(chapter);
        question.setTopic(topic);
        question.setSubject(topic.getSubject());
        question.setQuestionText(request.getQuestionText());

        Question updatedQuestion =
                questionRepository.save(question);


        // Update MCQ configuration
        mcqQuestion.setQuestionType(
                request.getQuestionType()
        );

        mcqQuestion.setMarks(
                request.getMarks()
        );

        McqQuestion updatedMcqQuestion =
                mcqQuestionRepository.save(mcqQuestion);


        // Remove old options
// Get existing active options
        List<McqOption> existingOptions =
                mcqOptionRepository
                        .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                questionId
                        );

        // Move existing options to temporary order values
        for (McqOption option : existingOptions) {
            // Use a value unique to this option. A fixed sequence beginning at
            // 10000 can collide with a previously soft-deleted option because
            // the database unique constraint still includes inactive rows.
            option.setOptionOrder(
                    Integer.MAX_VALUE - Math.toIntExact(option.getOptionId())
            );
        }

        mcqOptionRepository.saveAll(existingOptions);
        mcqOptionRepository.flush();


// Store option IDs received from frontend
        List<Long> requestOptionIds =
                request.getOptions()
                        .stream()
                        .map(McqOptionDTO::getOptionId)
                        .filter(optionId -> optionId != null)
                        .collect(Collectors.toList());


// Deactivate options removed from the request
        existingOptions.forEach(option -> {

            if (!requestOptionIds.contains(option.getOptionId())) {

                option.setActiveRow(false);

            }

        });

        mcqOptionRepository.saveAll(existingOptions);


// List for final response
        List<McqOption> savedOptions =
                new java.util.ArrayList<>();


// Update existing options or create new options
        for (McqOptionDTO optionDTO : request.getOptions()) {

            // ============================
            // EXISTING OPTION → UPDATE
            // ============================
            if (optionDTO.getOptionId() != null) {

                McqOption option =
                        existingOptions.stream()
                                .filter(existingOption ->
                                        existingOption.getOptionId()
                                                .equals(
                                                        optionDTO.getOptionId()
                                                )
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Option not found for this question"
                                        )
                                );


                option.setOptionOrder(
                        optionDTO.getOptionOrder()
                );

                option.setOptionText(
                        optionDTO.getOptionText()
                );

                option.setIsCorrect(
                        optionDTO.getIsCorrect()
                );

                option.setActiveRow(true);


                McqOption updatedOption =
                        mcqOptionRepository.save(option);

                savedOptions.add(updatedOption);
            }


            // ============================
            // NEW OPTION → CREATE
            // ============================
            else {

                McqOption newOption =
                        createOption(
                                questionId,
                                optionDTO
                        );

                McqOption savedOption =
                        mcqOptionRepository.save(newOption);

                savedOptions.add(savedOption);
            }
        }

        return buildResponse(
                updatedQuestion,
                updatedMcqQuestion,
                savedOptions
        );
    }


    // ==========================================
    // 6. DELETE MCQ (SOFT DELETE)
    // ==========================================

    @Transactional
    public void deleteMcqQuestion(
            Long questionId
    ) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"
                                )
                        );


        McqQuestion mcqQuestion =
                mcqQuestionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "MCQ question not found"
                                )
                        );


        // Deactivate main question
        question.setActiveRow(false);

        questionRepository.save(question);


        // Deactivate MCQ configuration
        mcqQuestion.setActiveRow(false);

        mcqQuestionRepository.save(mcqQuestion);


        // Deactivate all options
        List<McqOption> options =
                mcqOptionRepository
                        .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                questionId
                        );

        options.forEach(option ->
                option.setActiveRow(false)
        );

        mcqOptionRepository.saveAll(options);
    }

    // ==========================================
// 7. SUBMIT MCQ ANSWERS
// ==========================================

    @Transactional
    public McqSubmissionResponseDTO submitMcqAnswers(
            McqSubmissionRequestDTO request
    ) {

        // ------------------------------------------
        // Validate request
        // ------------------------------------------

        if (request == null || request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }

        if (request.getAnswers() == null) {
            throw new RuntimeException("Answers are required");
        }

        // ------------------------------------------
        // Validate user
        // ------------------------------------------

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        BigDecimal totalScore = BigDecimal.ZERO;

        List<McqSubmissionResultDTO> results =
                new ArrayList<>();

        // ------------------------------------------
        // Process every submitted question
        // ------------------------------------------

        for (McqAnswerSubmissionDTO submission :
                request.getAnswers()) {

            Long questionId =
                    submission.getQuestionId();

            if (questionId == null) {
                throw new RuntimeException(
                        "Question ID is required"
                );
            }

            // ------------------------------------------
            // Validate question
            // ------------------------------------------

            Question question =
                    questionRepository.findById(questionId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Question not found: "
                                                    + questionId
                                    )
                            );

            if (!Boolean.TRUE.equals(
                    question.getActiveRow()
            )) {
                throw new RuntimeException(
                        "Question is inactive: "
                                + questionId
                );
            }

            // ------------------------------------------
            // Validate MCQ configuration
            // ------------------------------------------

            McqQuestion mcqQuestion =
                    mcqQuestionRepository.findById(questionId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "MCQ question not found: "
                                                    + questionId
                                    )
                            );

            if (!Boolean.TRUE.equals(
                    mcqQuestion.getActiveRow()
            )) {
                throw new RuntimeException(
                        "MCQ question is inactive: "
                                + questionId
                );
            }

            // ------------------------------------------
            // Selected options
            // ------------------------------------------

            List<Long> selectedOptionIds =
                    submission.getSelectedOptionIds();

            if (selectedOptionIds == null) {
                selectedOptionIds = new ArrayList<>();
            }

            // Remove duplicate option IDs
            Set<Long> selectedIds =
                    new HashSet<>(selectedOptionIds);

            // ------------------------------------------
            // UNANSWERED
            // ------------------------------------------

            if (selectedIds.isEmpty()) {

                McqSubmissionResultDTO result =
                        new McqSubmissionResultDTO();

                result.setQuestionId(questionId);
                result.setSelectedOptionIds(
                        new ArrayList<>()
                );
                result.setCorrectOptionIds(
                        new ArrayList<>()
                );
                result.setStatus("UNANSWERED");
                result.setMarksAwarded(
                        BigDecimal.ZERO
                );

                results.add(result);

                // IMPORTANT:
                // Do NOT insert into answer_events
                // Do NOT insert into question_answers

                continue;
            }

            // ------------------------------------------
            // Get active options
            // ------------------------------------------

            List<McqOption> activeOptions =
                    mcqOptionRepository
                            .findByQuestionIdAndActiveRowTrueOrderByOptionOrderAsc(
                                    questionId
                            );

            // ------------------------------------------
            // Validate selected option IDs
            // ------------------------------------------

            Set<Long> validOptionIds =
                    activeOptions.stream()
                            .map(McqOption::getOptionId)
                            .collect(Collectors.toSet());

            for (Long selectedOptionId : selectedIds) {

                if (!validOptionIds.contains(
                        selectedOptionId
                )) {

                    throw new RuntimeException(
                            "Invalid option "
                                    + selectedOptionId
                                    + " for question "
                                    + questionId
                    );
                }
            }

            // ------------------------------------------
            // Get correct option IDs
            // ------------------------------------------

            Set<Long> correctIds =
                    activeOptions.stream()
                            .filter(option ->
                                    Boolean.TRUE.equals(
                                            option.getIsCorrect()
                                    )
                            )
                            .map(McqOption::getOptionId)
                            .collect(Collectors.toSet());

            // ------------------------------------------
            // Compare sets
            //
            // Works for:
            // SINGLE_CHOICE
            // MULTIPLE_CHOICE
            // ------------------------------------------

            boolean isCorrect =
                    selectedIds.equals(correctIds);

            String status =
                    isCorrect
                            ? "CORRECT"
                            : "WRONG";

            BigDecimal marksAwarded =
                    isCorrect
                            ? BigDecimal.valueOf(
                            mcqQuestion.getMarks()
                    )
                            : BigDecimal.ZERO;

            // ------------------------------------------
            // Get selected option text
            // ------------------------------------------

            String selectedAnswerText =
                    activeOptions.stream()
                            .filter(option ->
                                    selectedIds.contains(
                                            option.getOptionId()
                                    )
                            )
                            .map(McqOption::getOptionText)
                            .collect(Collectors.joining(", "));

            // ------------------------------------------
            // Get correct option text
            // ------------------------------------------

            String correctAnswerText =
                    activeOptions.stream()
                            .filter(option ->
                                    correctIds.contains(
                                            option.getOptionId()
                                    )
                            )
                            .map(McqOption::getOptionText)
                            .collect(Collectors.joining(", "));

            // ------------------------------------------
            // Attempt number
            // ------------------------------------------

            long previousAttempts =
                    answerEventRepository
                            .countByUser_UserIdAndQuestion_QuestionIdAndEventTypeAndActiveRowTrue(
                                    user.getUserId(),
                                    questionId,
                                    "MCQ_ANSWER"
                            );

            int attemptNumber =
                    Math.toIntExact(previousAttempts + 1);

            // ------------------------------------------
            // SAVE ANSWER EVENT
            //
            // Both correct and wrong answers
            // go into answer_events
            // ------------------------------------------

            AnswerEvent answerEvent =
                    new AnswerEvent();

            answerEvent.setUser(user);
            answerEvent.setQuestion(question);

            // MCQ does not use accounting attribute
            answerEvent.setAttribute(null);

            answerEvent.setEventType(
                    "MCQ_ANSWER"
            );

            answerEvent.setIsCorrect(
                    isCorrect
            );

            answerEvent.setAttemptNumber(
                    attemptNumber
            );

            answerEvent.setMarks(
                    marksAwarded
            );

            answerEvent.setUserAnswer(
                    selectedAnswerText
            );

            answerEvent.setDescription(
                    "MCQ attempt"
            );

            answerEvent.setActiveRow(true);

            /*
             * Store selected option ID.
             *
             * For multiple-choice questions there can be
             * multiple IDs, so the answer text remains the
             * complete selected answer while option_id stores
             * the first selected option.
             */
            answerEvent.setOptionId(
                    selectedIds.iterator().next()
            );

            answerEventRepository.save(
                    answerEvent
            );

            // ------------------------------------------
            // CORRECT ANSWER
            //
            // Save additionally to question_answers
            // ------------------------------------------

            if (isCorrect) {

                QuestionAnswer questionAnswer =
                        new QuestionAnswer();

                questionAnswer.setUser(user);
                questionAnswer.setQuestion(question);

                // Existing accounting fields remain NULL
                questionAnswer.setTableName(null);
                questionAnswer.setHeader(null);
                questionAnswer.setAttribute(null);
                questionAnswer.setPairAttribute(null);

                questionAnswer.setOptionId(
                        selectedIds.iterator().next()
                );

                questionAnswer.setAnswerText(
                        selectedAnswerText
                );

                questionAnswer.setActiveRow(true);
                questionAnswer.setRowStatus(1);

                questionAnswerRepository.save(
                        questionAnswer
                );

                totalScore =
                        totalScore.add(marksAwarded);
            }

            // ------------------------------------------
            // Build result
            // ------------------------------------------

            McqSubmissionResultDTO result =
                    new McqSubmissionResultDTO();

            result.setQuestionId(questionId);

            result.setSelectedOptionIds(
                    new ArrayList<>(selectedIds)
            );

            result.setCorrectOptionIds(
                    new ArrayList<>(correctIds)
            );

            result.setStatus(status);

            result.setMarksAwarded(
                    marksAwarded
            );

            results.add(result);
        }

        // ------------------------------------------
        // Final response
        // ------------------------------------------

        McqSubmissionResponseDTO response =
                new McqSubmissionResponseDTO();

        response.setScore(totalScore);
        response.setResults(results);

        return response;
    }


    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Course getCourse(Long courseId) {

        return courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found"
                        )
                );
    }


    private Chapter getChapter(Long chapterId) {

        return chapterRepository.findById(chapterId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Chapter not found"
                        )
                );
    }


    private Topic getTopic(Long topicId) {

        return topicRepository
                .findById(topicId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question topic not found"
                        )
                );
    }


    private McqOption createOption(
            Long questionId,
            McqOptionDTO optionDTO
    ) {

        McqOption option = new McqOption();

        option.setQuestionId(questionId);

        option.setOptionOrder(
                optionDTO.getOptionOrder()
        );

        option.setOptionText(
                optionDTO.getOptionText()
        );

        option.setIsCorrect(
                optionDTO.getIsCorrect()
        );

        option.setActiveRow(true);

        return option;
    }


    private McqOptionDTO mapOptionToDTO(
            McqOption option
    ) {

        McqOptionDTO dto =
                new McqOptionDTO();

        dto.setOptionId(
                option.getOptionId()
        );

        dto.setOptionOrder(
                option.getOptionOrder()
        );

        dto.setOptionText(
                option.getOptionText()
        );

        dto.setIsCorrect(
                option.getIsCorrect()
        );

        return dto;
    }


    private McqQuestionResponseDTO buildResponse(
            Question question,
            McqQuestion mcqQuestion,
            List<McqOption> options
    ) {

        McqQuestionResponseDTO response =
                new McqQuestionResponseDTO();


        // Question
        response.setQuestionId(
                question.getQuestionId()
        );

        response.setQuestionText(
                question.getQuestionText()
        );


        // Course
        response.setCourseId(
                question.getCourse().getCourseId()
        );

        response.setCourseName(
                question.getCourse().getName()
        );


        // Chapter
        response.setChapterId(
                question.getChapter().getChapterId()
        );

        response.setChapterName(
                question.getChapter().getName()
        );


        // Topic
        response.setTopicId(
                question.getTopic().getTopicId()
        );

        response.setTopicName(
                question.getTopic().getName()
        );


        // MCQ configuration
        response.setQuestionType(
                mcqQuestion.getQuestionType()
        );

        response.setMarks(
                mcqQuestion.getMarks()
        );


        // Options
        List<McqOptionDTO> optionDTOs =
                options.stream()
                        .map(this::mapOptionToDTO)
                        .collect(Collectors.toList());

        response.setOptions(optionDTOs);

        return response;
    }
}
