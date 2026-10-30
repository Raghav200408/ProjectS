package com.project.ProjectS.processor;

import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import com.project.ProjectS.mapper.SubjectExcelMapper;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.SubjectRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class SubjectExcelProcessor {
    private final SubjectExcelMapper mapper;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    public SubjectExcelProcessor(SubjectExcelMapper mapper, CourseRepository courseRepository,
                                 SubjectRepository subjectRepository) {
        this.mapper = mapper;
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public String process(List<Map<String, String>> rows, Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("A valid courseId is required");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Selected course not found"));
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("The workbook must contain a header and at least one subject row");
        }
        List<Subject> subjects = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int skipped = 0;
        // Validate the whole workbook before saving; failed files must not be partly imported.
        for (Map<String, String> row : rows) {
            Subject subject = mapper.map(row, course);
            String key = subject.getSubjectName().toLowerCase(Locale.ROOT);
            if (!seen.add(key) || subjectRepository.existsBySubjectNameIgnoreCaseAndCourse(
                    subject.getSubjectName(), course)) {
                skipped++;
                continue;
            }
            subjects.add(subject);
        }
        subjectRepository.saveAll(subjects);
        return "Subjects uploaded: " + subjects.size() + ". Duplicates skipped: " + skipped + ".";
    }
}
