package com.project.ProjectS.repository;

import com.project.ProjectS.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer,Long> {
    List<QuestionAnswer> findByActiveRowTrue();


    List<QuestionAnswer> findByQuestion_QuestionIdAndActiveRowTrue(
            Long questionId
    );
    @Modifying
    @Query("""
    UPDATE QuestionAnswer qa
    SET qa.activeRow = false
    WHERE qa.question.questionId = :questionId
      AND qa.activeRow = true
""")
    int  deactivateByQuestionId( @Param("questionId") Long questionId);
}
