package com.project.ProjectS.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.CourseDTO;
import com.project.ProjectS.repository.CourseDAO;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class CourseService {

    @Autowired
    private CourseDAO courseDAO;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public CourseDTO saveCourse(CourseDTO course) {
        return courseDAO.save(course);
    }

    public List<CourseDTO> getAllCourses() {

        String sql = """
            SELECT
                c.course_id,
                c.branch_id,
                b.branch_name,
                col.institute_name AS college_name,
                c.course_name,
                c.description
            FROM course c
            INNER JOIN branch b
                ON c.branch_id = b.branch_id
            INNER JOIN college col
                ON b.college_id = col.college_id
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            CourseDTO course = new CourseDTO();

            course.setCourseId(rs.getLong("course_id"));
            course.setBranchId(rs.getLong("branch_id"));
            course.setBranchName(rs.getString("branch_name"));
            course.setCollegeName(rs.getString("college_name"));
            course.setCourseName(rs.getString("course_name"));
            course.setDescription(rs.getString("description"));

            return course;
        });
    }

    public CourseDTO getCourseById(Long id) {

        String sql = """
            SELECT
                c.course_id,
                c.branch_id,
                b.branch_name,
                col.institute_name AS college_name,
                c.course_name,
                c.description
            FROM course c
            INNER JOIN branch b
                ON c.branch_id = b.branch_id
            INNER JOIN college col
                ON b.college_id = col.college_id
            WHERE c.course_id = ?
            """;

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {

            CourseDTO course = new CourseDTO();

            course.setCourseId(rs.getLong("course_id"));
            course.setBranchId(rs.getLong("branch_id"));
            course.setBranchName(rs.getString("branch_name"));
            course.setCollegeName(rs.getString("college_name"));
            course.setCourseName(rs.getString("course_name"));
            course.setDescription(rs.getString("description"));

            return course;
        });
    }

    public CourseDTO updateCourse(CourseDTO course) {

        courseDAO.save(course);

        return getCourseById(course.getCourseId());
    }

    public void deleteCourse(Long id) {
        courseDAO.deleteById(id);
    }
}