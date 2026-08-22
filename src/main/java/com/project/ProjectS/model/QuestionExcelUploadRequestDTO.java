package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class QuestionExcelUploadRequestDTO {
    private Integer categoryId;
    private Integer courseId;
    private Integer chapterId;
}
