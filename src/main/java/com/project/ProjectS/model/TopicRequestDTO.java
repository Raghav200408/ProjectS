package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicRequestDTO {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Chapter ID is required")
    private Long chapterId;

    @NotBlank(message = "Topic name is required")
    private String name;

    private Boolean activeRow;

}
