package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

// One chapter's share of one exam attempt: marks scored in that chapter,
// out of that chapter's own maximum on this paper.
@Getter
@Setter
public class ChapterMarksDTO {

    private Long chapterId;
    private String chapterName;
    private Double scoredMarks;
    private Double maxMarks;
}
