package com.project.ProjectS.model;

/** ANSWERED means every required practice answer is correct; partial work remains pending. */
public record QuestionAnswerStatusDTO(long questionId, String status,
        long requiredAnswers, long savedAnswers) {}
