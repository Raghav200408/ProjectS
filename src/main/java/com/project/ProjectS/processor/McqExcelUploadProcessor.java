package com.project.ProjectS.processor;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.QuestionExcelUploadResponseDTO;
import com.project.ProjectS.model.QuestionUploadErrorDTO;
import com.project.ProjectS.repository.*;
import com.project.ProjectS.service.ExcelUploadService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class McqExcelUploadProcessor {
    private static final List<String> REQUIRED_HEADERS = List.of(
            "question_text", "question_type", "marks", "option_1", "option_2",
            "option_3", "option_4", "correct_option");
    private final QuestionRepository questionRepository;
    private final McqQuestionRepository mcqQuestionRepository;
    private final McqOptionRepository mcqOptionRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final QuestionTypeRepository questionTypeRepository;
    private final ExcelUploadService excelUploadService;

    public McqExcelUploadProcessor(QuestionRepository questionRepository,
            McqQuestionRepository mcqQuestionRepository, McqOptionRepository mcqOptionRepository,
            CourseRepository courseRepository, ChapterRepository chapterRepository,
            TopicRepository topicRepository, QuestionTypeRepository questionTypeRepository,
            ExcelUploadService excelUploadService) {
        this.questionRepository = questionRepository;
        this.mcqQuestionRepository = mcqQuestionRepository;
        this.mcqOptionRepository = mcqOptionRepository;
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.topicRepository = topicRepository;
        this.questionTypeRepository = questionTypeRepository;
        this.excelUploadService = excelUploadService;
    }

    @Transactional(rollbackFor = Exception.class)
    public QuestionExcelUploadResponseDTO processExcel(
            MultipartFile file, Long courseId, Long chapterId, Long topicId) throws IOException {
        if (courseId == null || chapterId == null || topicId == null) {
            throw new IllegalArgumentException("Course, chapter and topic are required");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        validateHierarchy(courseId, chapterId, chapter, topic);

        List<Map<String, String>> rows = excelUploadService.readExcel(file);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Excel must contain a header and at least one question");
        }
        if (!rows.get(0).keySet().containsAll(REQUIRED_HEADERS)) {
            throw new IllegalArgumentException("Required Excel columns: " + String.join(", ", REQUIRED_HEADERS));
        }
        QuestionExcelUploadResponseDTO result = new QuestionExcelUploadResponseDTO();
        Map<String, Question> existing = new HashMap<>();
        for (Question question : questionRepository
                .findByCourse_CourseIdAndChapter_ChapterIdAndTopic_TopicIdAndActiveRowTrue(
                        courseId, chapterId, topicId)) {
            existing.putIfAbsent(question.getQuestionText().trim().toLowerCase(Locale.ROOT), question);
        }

        for (Map<String, String> row : rows) {
            if (row.entrySet().stream().filter(entry -> !entry.getKey().startsWith("_"))
                    .allMatch(entry -> entry.getValue() == null || entry.getValue().isBlank())) {
                continue;
            }
            result.setTotalRows(result.getTotalRows() + 1);
            ValidatedRow validated;
            try {
                validated = validateRow(row, topic);
            } catch (IllegalArgumentException ex) {
                QuestionUploadErrorDTO error = new QuestionUploadErrorDTO();
                error.setRowNumber(Integer.parseInt(row.get("_excel_row_number")));
                error.setQuestionText(row.get("question_text"));
                error.setErrorMessage(ex.getMessage());
                result.getErrors().add(error);
                result.setFailedQuestions(result.getFailedQuestions() + 1);
                continue;
            }
            String key = validated.text().toLowerCase(Locale.ROOT);
            Question question = existing.get(key);
            if (question != null && mcqQuestionRepository.existsById(question.getQuestionId())) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }
            // Persistence failures must escape so the transaction rolls back, not report success.
            if (question == null) {
                question = new Question();
                question.setCourse(course);
                question.setChapter(chapter);
                question.setTopic(topic);
                question.setQuestionText(validated.text());
                question.setActiveRow(true);
            }
            question.setSubject(topic.getSubject());
            question.setQuestionType(validated.type());
            question = questionRepository.save(question);
            existing.put(key, question);

            McqQuestion mcq = new McqQuestion();
            mcq.setQuestionId(question.getQuestionId());
            mcq.setQuestionType(validated.type());
            mcq.setMarks(validated.marks());
            mcq.setActiveRow(true);
            mcqQuestionRepository.save(mcq);
            List<McqOption> options = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                McqOption option = new McqOption();
                option.setQuestionId(question.getQuestionId());
                option.setOptionOrder(i);
                option.setOptionText(row.get("option_" + i).trim());
                option.setIsCorrect(validated.correctOptions().contains(i));
                option.setActiveRow(true);
                options.add(option);
            }
            mcqOptionRepository.saveAll(options);
            result.setUploadedQuestions(result.getUploadedQuestions() + 1);
        }
        result.setSuccess(result.getTotalRows() > 0 && result.getFailedQuestions() == 0);
        result.setMessage(result.getUploadedQuestions() + " MCQ questions uploaded, "
                + result.getSkippedRows() + " duplicates skipped, "
                + result.getFailedQuestions() + " failed");
        return result;
    }

    static void validateHierarchy(Long courseId, Long chapterId, Chapter chapter, Topic topic) {
        if (chapter.getCourse() == null || !courseId.equals(chapter.getCourse().getCourseId())
                || topic.getCourse() == null || !courseId.equals(topic.getCourse().getCourseId())
                || topic.getChapter() == null || !chapterId.equals(topic.getChapter().getChapterId())
                || topic.getSubject() == null || chapter.getSubject() == null
                || !Objects.equals(topic.getSubject().getSubjectId(), chapter.getSubject().getSubjectId())) {
            throw new IllegalArgumentException("Selected course, chapter, topic and subject do not match");
        }
    }

    private ValidatedRow validateRow(Map<String, String> row, Topic topic) {
        String text = required(row, "question_text");
        String type = required(row, "question_type").toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
        if (!Set.of("SINGLE_CHOICE", "MULTIPLE_CHOICE").contains(type)) {
            throw new IllegalArgumentException("question_type must be SINGLE_CHOICE or MULTIPLE_CHOICE");
        }
        double marks;
        try {
            marks = Double.parseDouble(required(row, "marks"));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("marks must be a positive number");
        }
        if (!Double.isFinite(marks) || marks <= 0) {
            throw new IllegalArgumentException("marks must be a positive number");
        }
        for (int i = 1; i <= 4; i++) {
            required(row, "option_" + i);
        }
        Set<Integer> correct = parseCorrectOptions(required(row, "correct_option"), type);
        if (row.containsKey("subject_name")) {
            validateSubject(row.get("subject_name"), topic);
        }
        String name = type.equals("SINGLE_CHOICE") ? "MCQ Single Choice" : "MCQ Multiple Choice";
        QuestionType questionType = questionTypeRepository.findByQuestionType(name)
                .orElseThrow(() -> new IllegalArgumentException("Question type not configured: " + name));
        return new ValidatedRow(text, questionType, marks, correct);
    }

    private static String required(Map<String, String> row, String field) {
        String value = row.get(field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    static Set<Integer> parseCorrectOptions(String value, String questionType) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Correct option is required");
        }
        Set<Integer> options = new LinkedHashSet<>();
        for (String token : value.split(",", -1)) {
            int option;
            try {
                option = Integer.parseInt(token.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Correct option must contain numbers from 1 to 4, separated by commas");
            }
            if (option < 1 || option > 4) {
                throw new IllegalArgumentException("Correct option must be between 1 and 4");
            }
            if (!options.add(option)) {
                throw new IllegalArgumentException("Duplicate correct option: " + option);
            }
        }
        if ("SINGLE_CHOICE".equals(questionType) && options.size() != 1) {
            throw new IllegalArgumentException("SINGLE_CHOICE requires exactly one correct option");
        }
        return options;
    }

    static void validateSubject(String subjectName, Topic topic) {
        if (subjectName == null || subjectName.isBlank()) {
            throw new IllegalArgumentException("subject_name is required");
        }
        if (topic.getSubject() == null || topic.getSubject().getSubjectName() == null
                || !topic.getSubject().getSubjectName().trim().equalsIgnoreCase(subjectName.trim())) {
            throw new IllegalArgumentException("subject_name must match the subject of the selected topic");
        }
    }

    private record ValidatedRow(String text, QuestionType type, double marks, Set<Integer> correctOptions) {}
}
