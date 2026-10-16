package com.project.ProjectS.model;

/** Answered means saved, not necessarily correct; grading is reported separately. */
public record QuestionAnswerStatusDTO(long questionId, String status,
        long requiredAnswers, long savedAnswers) {}
