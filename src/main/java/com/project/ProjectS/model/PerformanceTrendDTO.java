package com.project.ProjectS.model;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PerformanceTrendDTO {

    private String examName;
    private Double averagePercentage;
    private LocalDateTime examDate;
}
