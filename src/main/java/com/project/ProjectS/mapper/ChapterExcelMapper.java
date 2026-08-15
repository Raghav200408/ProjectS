package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class ChapterExcelMapper implements ExcelRowMapper<Chapter> {
    @Autowired
    public ChapterExcelMapper(BranchRepository branchRepository, CourseRepository courseRepository) {
        this.branchRepository = branchRepository;
        this.courseRepository = courseRepository;
    }

    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;



    @Override
    public Chapter map(Map<String,String> row) {


        String branchName =
                row.get("branch_name");


        String courseName =
                row.get("course_name");


        String chapterName =
                row.get("chapter_name");



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



        // Create Chapter Entity

        Chapter chapter = new Chapter();


        chapter.setCourse(course);


        chapter.setName(chapterName);



        return chapter;

    }
}
