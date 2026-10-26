package com.project.ProjectS.processor;


import com.project.ProjectS.entity.Course;
import com.project.ProjectS.mapper.CourseExcelMapper;
import com.project.ProjectS.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class CourseExcelProcessor implements ExcelProcessor {
    @Autowired
    public CourseExcelProcessor(CourseExcelMapper courseExcelMapper, CourseRepository courseRepository) {
        this.courseExcelMapper = courseExcelMapper;
        this.courseRepository = courseRepository;
    }

    private final CourseExcelMapper courseExcelMapper;
    private final CourseRepository courseRepository;
    @Autowired private com.project.ProjectS.service.CourseAssignmentService assignments;



    @Override
    public void process(List<Map<String, String>> excelData) {


        for(Map<String,String> row : excelData){


            if(row.values()
                    .stream()
                    .allMatch(String::isBlank)){

                continue;
            }



            Course course =
                    courseExcelMapper.map(row);



            var branch = course.getBranch();
            String requestedId = row.get("course_id");
            if (requestedId != null && !requestedId.isBlank()) {
                course = courseRepository.findById(Long.valueOf(requestedId.trim()))
                        .orElseThrow(() -> new IllegalArgumentException("Course ID not found"));
            } else {
                var existing = courseRepository.findByNameIgnoreCase(course.getName());
                if (existing.size() > 1) throw new IllegalArgumentException("Course name is ambiguous; supply course_id");
                if (existing.size() == 1) course = existing.get(0);
                else {
                    course.setBranch(null); course.setCollege(null);
                    courseRepository.save(course);
                }
            }
            if (branch != null) assignments.saveAssignment(course, branch, true);

        }

    }

}
