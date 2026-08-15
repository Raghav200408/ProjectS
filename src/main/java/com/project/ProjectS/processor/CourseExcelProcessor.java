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



            boolean exists =
                    courseRepository.existsByNameAndBranch(
                            course.getName(),
                            course.getBranch()
                    );



            if(exists){

                continue;

            }



            courseRepository.save(course);

        }

    }

}
