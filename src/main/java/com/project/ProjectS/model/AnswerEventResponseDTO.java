package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AnswerEventResponseDTO {

    private Long answerEventId;
    private Long optionId;

    private Long userId;
    private String username;

    private Long questionId;

    private Long chapterId;
    private String chapterName;

    private Long topicId;
    private String topicName;

    private String questionText;
   // private String questionType;


    private Long attributeId;
    private Long questionAttributeId;
    private String attributeName;


    private Integer answerPosition;

    private String arithmetic;

    private String eventType;

    private Boolean isCorrect;

    private Integer attemptNumber;

    private BigDecimal marks;

    private String hint;

    private String description;

    private String userAnswer;

    private Boolean activeRow;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
