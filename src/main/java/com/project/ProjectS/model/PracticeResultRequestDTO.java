package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

/**
 * One attempted unit of a practice question. The frontend decides isCorrect;
 * the backend stores it as sent and only validates the structure and that the
 * hierarchy ids really belong to the question.
 */
@Getter
@Setter
public class PracticeResultRequestDTO {

    // Optional. The logged-in user is always the one recorded; if this is
    // sent it must match, so one student can't write another's results.
    private Long userId;

    private Long questionId;

    // The question_attributes row that was attempted. Required for
    // "ATTRIBUTE" questions.
    private Long questionAttributeId;

    private Long courseId;
    private Long subjectId;
    private Long chapterId;
    private Long topicId;

    // "ATTRIBUTE" (Journal / Dropdown / Drag-and-drop). MCQ results are
    // recorded by the MCQ submit endpoint, not here.
    private String questionType;

    private Boolean isCorrect;

    // --- optional: what the historical answer_events row should carry ---
    // "ANSWER" (default), "HINT" (answered after using the hint) or
    // "AUTOFILL" (the app filled it in - saved as an event but not counted).
    private String eventType;

    private Integer answerPosition;

    private String arithmetic;

    private String hint;

    private String description;

    private String userAnswer;
}
