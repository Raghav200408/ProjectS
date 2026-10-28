package com.project.ProjectS.mapper;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.Course;
import com.project.ProjectS.entity.Subject;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CourseRepository;
import com.project.ProjectS.repository.CourseAssignmentRepository;
import com.project.ProjectS.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class ChapterExcelMapper implements ExcelRowMapper<Chapter> {
    @Autowired
    public ChapterExcelMapper(BranchRepository branchRepository, CourseRepository courseRepository, SubjectRepository subjectRepository, CourseAssignmentRepository courseAssignmentRepository) {
        this.branchRepository = branchRepository;
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
    }

    private final BranchRepository branchRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;



    @Override
    public Chapter map(Map<String,String> row) {


        String branchName =
                row.get("branch_name");


        String courseName =
                row.get("course_name");


        String chapterName =
                row.get("chapter_name");

        String subjectName = row.get("subject_name");



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



        // Resolve shared catalog content through the selected branch assignment.
        var matches = courseAssignmentRepository.findActiveForScope(
                        branch.getCollege().getCollegeId(), branch.getBranchId())
                .stream()
                .map(assignment -> assignment.getCourse())
                .filter(candidate -> candidate.getName().equalsIgnoreCase(courseName.trim()))
                .distinct()
                .toList();
        if (matches.size() != 1) {
            throw new RuntimeException(matches.isEmpty()
                    ? "Course not assigned to branch: " + courseName
                    : "Multiple catalog courses match " + courseName + " for branch " + branch.getBranchName());
        }
        Course course = matches.get(0);



        // Create Chapter Entity

        Chapter chapter = new Chapter();


        chapter.setCourse(course);

        Subject subject = subjectRepository.findBySubjectNameAndCourse(subjectName, course)
                .orElseThrow(() -> new RuntimeException("Subject not found: " + subjectName));
        chapter.setSubject(subject);


        chapter.setName(chapterName);



        return chapter;

    }
}
