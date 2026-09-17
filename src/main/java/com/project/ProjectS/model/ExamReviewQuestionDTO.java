package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * One question's slice of an exam/mock-exam review: what was submitted for it
 * and whether it was fully correct. {@code answers} reuses the same shape the
 * candidate originally submitted ({@code answeredData} rows), so the review
 * screen's read-only tables can render it exactly like the live attempt does.
 */
@Getter
@Setter
public class ExamReviewQuestionDTO {

    private Long questionId;

    private String questionType;

    private Boolean correct;

    private List<ExamAnswerDTO> answers;

    // MCQ only: the question's correct option id(s), so the review screen can
    // highlight them. Never sent on the live exam-taking payload - only this
    // post-submission review response reveals it.
    private List<Long> correctOptionIds;
}
