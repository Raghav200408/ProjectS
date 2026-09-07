package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class BulkSubscriptionAssignmentDTO {
    private Long courseId;
    private Long planId;
    private List<Long> studentIds;
}
