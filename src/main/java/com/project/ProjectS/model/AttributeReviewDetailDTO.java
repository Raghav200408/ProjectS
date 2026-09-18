package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * What the review screen shows when a trial-balance / transaction row is
 * clicked: the attribute's own Rule Engine hint text(s), and each wrong line
 * this student submitted for it on this attempt (answer_events.description
 * where is_correct is false).
 */
@Getter
@Setter
public class AttributeReviewDetailDTO {

    private List<String> hints;

    private List<String> mistakes;
}
