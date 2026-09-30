package com.project.ProjectS.repository;

import com.project.ProjectS.entity.QuestionFillBlankAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionFillBlankAnswerRepository
        extends JpaRepository<QuestionFillBlankAnswer, Long> {

    List<QuestionFillBlankAnswer> findByQuestionQuestionIdOrderByDisplayOrderAsc(
            Long questionId
    );

    List<QuestionFillBlankAnswer>
    findByQuestionQuestionIdAndBlankNumberAndIsCorrectTrueOrderByDisplayOrderAsc(
            Long questionId,
            Integer blankNumber
    );

    void deleteByQuestionQuestionId(Long questionId);
}