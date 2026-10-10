package com.project.ProjectS.service;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdjustmentQuestionPersistenceTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existingQuestionApisPreserveTypeNotesBothAmountsAndAdjustmentIdentity(boolean adjustment) {
        QuestionRepository questions = mock(QuestionRepository.class);
        QuestionAttributeRepository rows = mock(QuestionAttributeRepository.class);
        CourseRepository courses = mock(CourseRepository.class);
        ChapterRepository chapters = mock(ChapterRepository.class);
        SubjectRepository subjects = mock(SubjectRepository.class);
        TopicRepository topics = mock(TopicRepository.class);
        QuestionTypeRepository types = mock(QuestionTypeRepository.class);
        TableHeaderRepository headers = mock(TableHeaderRepository.class);
        TableAttributeRepository attributes = mock(TableAttributeRepository.class);
        QuestionService service = new QuestionService(questions, rows, courses, chapters, subjects, topics,
                types, headers, attributes, null, null, mock(McqOptionRepository.class), null,
                mock(QuestionMatchingPairRepository.class), mock(QuestionFillBlankAnswerRepository.class), null);
        Course course = new Course(); course.setCourseId(1L);
        Subject subject = new Subject(); subject.setSubjectId(2L); subject.setCourse(course);
        Chapter chapter = new Chapter(); chapter.setChapterId(3L); chapter.setName("Final Accounts with Adjustments");
        chapter.setCourse(course); chapter.setSubject(subject);
        Topic topic = new Topic(); topic.setTopicId(4L); topic.setCourse(course); topic.setSubject(subject); topic.setChapter(chapter);
        QuestionType type = new QuestionType(); type.setQuestionTypeId(9L); type.setQuestionType("dragAndDropWithAdj");
        TableHeader header = new TableHeader(); header.setHeaderId(74L); header.setName("Credit Particulars");
        TableAttribute attribute = new TableAttribute(); attribute.setAttributeId(12L); attribute.setName("Outstanding Rent"); attribute.setTableHeader(header);
        when(courses.findById(1L)).thenReturn(Optional.of(course));
        when(subjects.findById(2L)).thenReturn(Optional.of(subject));
        when(chapters.findById(3L)).thenReturn(Optional.of(chapter));
        when(topics.findById(4L)).thenReturn(Optional.of(topic));
        when(types.findById(9L)).thenReturn(Optional.of(type));
        when(headers.findById(74L)).thenReturn(Optional.of(header));
        when(attributes.findById(12L)).thenReturn(Optional.of(attribute));
        List<QuestionAttribute> persisted = new ArrayList<>();
        when(questions.save(any())).thenAnswer(call -> {
            Question saved = call.getArgument(0); saved.setQuestionId(50L);
            when(questions.findById(50L)).thenReturn(Optional.of(saved)); return saved;
        });
        when(rows.save(any())).thenAnswer(call -> {
            QuestionAttribute saved = call.getArgument(0); saved.setQuestionAttributeId(101L);
            persisted.add(saved); return saved;
        });
        when(rows.findByQuestion_QuestionId(50L)).thenReturn(persisted);
        QuestionAttributeRequestDTO row = new QuestionAttributeRequestDTO(); row.setAttributeId(12L); row.setHeaderId(74L);
        row.setAmount(new BigDecimal("200.00")); row.setAmount2(new BigDecimal("70.00")); row.setNote("Rent unpaid");
        // Older clients omit the flag; the server preserves their balance behavior.
        row.setAdjustment(adjustment ? true : null);
        QuestionRequestDTO request = new QuestionRequestDTO(); request.setCourseId(1L); request.setSubjectId(2L);
        request.setChapterId(3L); request.setTopicId(4L); request.setQuestionTypeId(9L);
        request.setQuestionText("Prepare Final Accounts"); request.setQuestionAttributes(List.of(row));
        QuestionResponseDTO created = service.createQuestion(request);
        QuestionResponseDTO retrieved = service.getQuestionById(created.getQuestionId());
        assertEquals("dragAndDropWithAdj", retrieved.getQuestionType());
        QuestionAttributeResponseDTO restored = retrieved.getQuestionAttributes().get(0);
        assertEquals(adjustment, restored.getAdjustment());
        assertEquals(101L, restored.getQuestionAttributeId());
        assertEquals(new BigDecimal("200.00"), restored.getAmount());
        assertEquals(new BigDecimal("70.00"), restored.getAmount2());
        assertEquals("Rent unpaid", restored.getNote());
        assertEquals(74L, restored.getHeaderId());
    }
}
