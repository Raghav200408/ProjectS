package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PracticePerformanceResponseDTO {

    // "COURSE", "SUBJECT", "CHAPTER" or "TOPIC".
    private String level;

    // Students the metrics cover. Counted from users/roles, so a student who
    // hasn't attempted anything is still included.
    private long studentCount;

    // The same metrics over every item below, for the headline cards.
    private PracticePerformanceItemDTO summary;

    private List<PracticePerformanceItemDTO> items;
}
