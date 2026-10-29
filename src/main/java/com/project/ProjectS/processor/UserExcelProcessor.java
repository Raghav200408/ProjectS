package com.project.ProjectS.processor;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.mapper.UserExcelMapper;
import com.project.ProjectS.model.StudentExcelUploadErrorDTO;
import com.project.ProjectS.model.StudentExcelUploadResponseDTO;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@Component
public class UserExcelProcessor {
    private static final Logger log = LogManager.getLogger(UserExcelProcessor.class);
    @Autowired
    public UserExcelProcessor(UserExcelMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    private final UserExcelMapper userMapper;
    private final UserRepository userRepository;

    public StudentExcelUploadResponseDTO process(List<Map<String, String>> excelData) {
        StudentExcelUploadResponseDTO response = new StudentExcelUploadResponseDTO();

        for (int i = 0; i < excelData.size(); i++) {
            Map<String, String> row = excelData.get(i);

            // Skip empty rows
            if (row.values().stream().allMatch(value -> value == null || value.isBlank())) {
                continue;
            }

            int rowNumber = i + 2;
            try {
                rowNumber = Integer.parseInt(row.getOrDefault("_excel_row_number", String.valueOf(rowNumber)));
            } catch (NumberFormatException ignored) {
                // Fall back to the row's position in the parsed sheet.
            }
            response.setTotalRows(response.getTotalRows() + 1);
            try {
                User user = userMapper.map(row);
                userRepository.save(user);
                response.setStudentsUploaded(response.getStudentsUploaded() + 1);
            } catch (Exception e) {
                log.warn("Skipping invalid student Excel row {}: {}", rowNumber, e.getMessage());
                response.getErrors().add(new StudentExcelUploadErrorDTO(
                        rowNumber, "Excel row " + rowNumber + ": " + e.getMessage()));
            }
        }

        response.setSkippedRows(response.getErrors().size());
        response.setSuccess(response.getSkippedRows() == 0);
        response.setMessage(response.getStudentsUploaded() + " students uploaded, "
                + response.getSkippedRows() + " rows skipped."
                + (response.getSkippedRows() > 0 ? " Review the errors and retry only skipped rows." : ""));
        return response;
    }
}
