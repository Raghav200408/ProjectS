package com.project.ProjectS.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

    public void validate(MultipartFile file) {

        if (file == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !(fileName.endsWith(".xlsx")
                        || fileName.endsWith(".xls")
                        || fileName.endsWith(".xlsm"))) {

            throw new IllegalArgumentException(
                    "Only Excel files (.xlsx, .xls, .xlsm) are allowed."
            );
        }
    }
}