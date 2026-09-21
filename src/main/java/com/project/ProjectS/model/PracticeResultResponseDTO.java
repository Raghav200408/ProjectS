package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PracticeResultResponseDTO {

    // The historical answer_events row written for this attempt.
    private Long answerEventId;

    // False when the event was saved but doesn't count as an attempt
    // (AUTOFILL); the fields below are then null.
    private boolean resultRecorded;

    private Long practiceResultId;
    private Long userId;
    private Long questionId;
    private Long questionAttributeId;
    private Long courseId;
    private Long subjectId;
    private Long chapterId;
    private Long topicId;
    private String questionType;
    private Boolean isCorrect;
    private Integer attemptNumber;
    private LocalDateTime answeredAt;
}
