package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class QuestionResponseDTO {

    private Long questionId;


    private Long courseId;
    private String courseName;

    private Long subjectId;
    private String subjectName;


    private Long chapterId;
    private String chapterName;


    private Long topicId;
    private String topicName;

    private Long questionTypeId;
    private String questionType;


    private String questionText;


    private List<QuestionAttributeResponseDTO> questionAttributes;

    private List<McqOptionDTO> options;

    private Boolean activeRow;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<MatchingPairResponseDTO> pairs;

    private List<FillInTheBlankAnswerResponseDTO> blanks;

    // Exam and mock exam paper only (QuestionService.getQuestionsByIds).
    // MATCHING: the column B texts, shuffled; pairs then carry no columnB.
    private List<String> matchOptions;
    // FILL_IN_THE_BLANK: how many blanks to show, and every option text,
    // shuffled. blanks is empty on the paper, so no id, order or blank
    // number can give an answer away.
    private Integer blankCount;
    private List<String> wordBank;

}
