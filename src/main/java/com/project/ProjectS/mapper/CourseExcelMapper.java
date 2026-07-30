package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CourseExcelMapper implements ExcelRowMapper<Course> {


    @Autowired
    private BranchRepository branchRepository;


    @Override
    public Course map(Map<String, String> row) {


        List<Branch> branches =
                branchRepository.findByBranchName(
                        row.get("branch_name")
                );


        if(branches.isEmpty()) {

            throw new RuntimeException(
                    "Branch not found : "
                            + row.get("branch_name")
            );
        }


        Branch branch = branches.get(0);


        Course course = new Course();


        course.setBranch(branch);


        course.setName(
                row.get("course_name")
        );


        return course;
    }
}