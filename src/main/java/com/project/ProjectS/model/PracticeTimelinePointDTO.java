package com.project.ProjectS.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PracticeTimelinePointDTO {
    private LocalDate date;
    private long answerCount;
    private long correctCount;
}
