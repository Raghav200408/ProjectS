package com.project.ProjectS.mapper;


import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Section;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class SectionExcelMapper implements ExcelRowMapper<Section> {


    @Autowired
    private CourseRepository courseRepository;


    @Autowired
    private BranchRepository branchRepository;



    @Override
    public Section map(Map<String,String> row) {


        String branchName = row.get("branch_name");

        String courseName = row.get("course_name");


        // Find Branch

        Branch branch =
                branchRepository.findByBranchName(branchName)
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Branch not found : "
                                                + branchName
                                )
                        );


        // Find Course using Branch + Course Name

        Course course =
                courseRepository.findByNameAndBranch(
                                courseName,
                                branch
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found : "
                                                + courseName
                                )
                        );


        // Create Section

        Section section = new Section();


        section.setCourse(course);


        section.setSectionName(
                row.get("section_name")
        );


        section.setDescription(
                row.get("description")
        );


        return section;

    }

}