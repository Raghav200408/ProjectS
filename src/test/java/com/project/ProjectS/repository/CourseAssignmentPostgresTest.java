package com.project.ProjectS.repository;

import com.project.ProjectS.model.DashboardResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

// Opt-in only, against a newly initialized disposable server/database. Never
// reads application datasource settings or operates on an existing database.
@EnabledIfSystemProperty(named = "courseassignment.test.url", matches = "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/courseassignment_isolated")
class CourseAssignmentPostgresTest {
    @Test void migrationPreservesIdsAndScopesSharedCourseCountsWithoutMixingStudentProgress() throws Exception {
        String url = System.getProperty("courseassignment.test.url");
        try (var connection = DriverManager.getConnection(url, "courseassignment_test", ""); var sql = connection.createStatement()) {
            sql.execute("""
                CREATE TABLE college(college_id BIGINT PRIMARY KEY, active_row BOOLEAN);
                CREATE TABLE branch(branch_id BIGINT PRIMARY KEY, college_id BIGINT REFERENCES college, active_row BOOLEAN);
                CREATE TABLE courses(course_id BIGINT PRIMARY KEY, name TEXT, college_id BIGINT NOT NULL REFERENCES college,
                    branch_id BIGINT NOT NULL REFERENCES branch, active_row BOOLEAN);
                CREATE TABLE section(section_id BIGINT PRIMARY KEY, section_name TEXT, course_id BIGINT REFERENCES courses,
                    college_id BIGINT, branch_id BIGINT, active_row BOOLEAN, UNIQUE(course_id, section_name));
                CREATE TABLE users(user_id BIGINT PRIMARY KEY, college_id BIGINT, branch_id BIGINT);
                CREATE TABLE subscription_plans(plan_id BIGINT PRIMARY KEY, active BOOLEAN);
                CREATE TABLE user_subscriptions(subscription_id BIGINT PRIMARY KEY, user_id BIGINT REFERENCES users,
                    course_id BIGINT REFERENCES courses, plan_id BIGINT REFERENCES subscription_plans,
                    active BOOLEAN, starts_at TIMESTAMP, expires_at TIMESTAMP);
                CREATE TABLE questions(question_id BIGINT PRIMARY KEY, course_id BIGINT REFERENCES courses, active_row BOOLEAN);
                CREATE TABLE plan_courses(plan_course_id BIGINT PRIMARY KEY, course_id BIGINT REFERENCES courses,
                    plan_id BIGINT REFERENCES subscription_plans, all_course_questions BOOLEAN);
                CREATE TABLE plan_questions(plan_course_id BIGINT REFERENCES plan_courses, question_id BIGINT REFERENCES questions);
                CREATE TABLE mcq_questions(question_id BIGINT REFERENCES questions, active_row BOOLEAN);
                CREATE TABLE question_matching_pairs(question_id BIGINT, pair_id BIGINT);
                CREATE TABLE question_fill_blank_answers(question_id BIGINT, blank_number INTEGER);
                CREATE TABLE question_attributes(question_id BIGINT, attribute_id BIGINT, active_row BOOLEAN);
                CREATE TABLE rule_engines(attribute_id BIGINT, active_row BOOLEAN, arithmetic1 TEXT, arithmetic2 TEXT, arithmetic3 TEXT, arithmetic4 TEXT);
                CREATE TABLE practice_results(practice_result_id BIGINT, user_id BIGINT, question_id BIGINT, unit_key TEXT);
                INSERT INTO college VALUES (1,true),(2,true);
                INSERT INTO branch VALUES (10,1,true),(20,2,true),(30,2,true);
                INSERT INTO courses VALUES (101,'Existing populated course',1,10,true);
                INSERT INTO section VALUES (1,'Section A',101,1,10,true);
                INSERT INTO users VALUES (1,1,10),(2,2,20),(3,2,30),(4,2,20);
                INSERT INTO subscription_plans VALUES (1,true);
                INSERT INTO user_subscriptions VALUES
                    (1,1,101,1,true,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '10 days'),
                    (2,2,101,1,true,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '5 days'),
                    (3,3,101,1,true,CURRENT_TIMESTAMP - INTERVAL '1 day',CURRENT_TIMESTAMP + INTERVAL '5 days'),
                    (4,4,101,1,true,CURRENT_TIMESTAMP - INTERVAL '10 days',CURRENT_TIMESTAMP - INTERVAL '5 days');
                INSERT INTO questions VALUES (11,101,true);
                INSERT INTO plan_courses VALUES (1,101,1,true);
                INSERT INTO mcq_questions VALUES (11,true);
                INSERT INTO practice_results VALUES (1,1,11,'MCQ');
                """);
            String migration = Files.readString(Path.of("src/main/java/DataBase/courseassignment.sql"));
            sql.execute(migration);
            sql.execute(migration); // Re-running must neither duplicate nor replace assignments.
            try (var rows = sql.executeQuery("SELECT COUNT(*) FROM courseassignment WHERE course_id=101 AND branch_id=10")) {
                assertTrue(rows.next()); assertEquals(1, rows.getInt(1));
            }
            sql.execute("INSERT INTO courseassignment(course_id, branch_id) VALUES (101,20)");
            sql.execute("INSERT INTO section VALUES (2,'Section A',101,2,20,true)");
            sql.execute("INSERT INTO courses(course_id,name,active_row) VALUES (102,'New global course',true)");
            var dataSource = new DriverManagerDataSource(url, "courseassignment_test", "");
            var dashboard = new DashboardRepository(new NamedParameterJdbcTemplate(dataSource));
            DashboardResponseDTO collegeA = new DashboardResponseDTO(), collegeB = new DashboardResponseDTO();
            dashboard.populateCounts(collegeA, 1, new DashboardRepository.Scope(1L, 10L, false, false));
            dashboard.populateCounts(collegeB, 2, new DashboardRepository.Scope(2L, 20L, false, false));
            assertEquals(1, collegeA.getTotalCourses()); assertEquals(1, collegeB.getTotalCourses());
            DashboardResponseDTO studentA = new DashboardResponseDTO(), studentB = new DashboardResponseDTO();
            dashboard.populateCounts(studentA, 1, new DashboardRepository.Scope(1L, 10L, true, true));
            dashboard.populateCounts(studentB, 2, new DashboardRepository.Scope(2L, 20L, true, true));
            assertEquals(1, studentA.getMyCourses()); assertEquals(1, studentB.getMyCourses());
            assertEquals(1, studentA.getAttemptedUnits()); assertEquals(0, studentB.getAttemptedUnits());
            DashboardResponseDTO unassigned = new DashboardResponseDTO(), expired = new DashboardResponseDTO();
            dashboard.populateCounts(unassigned, 3, new DashboardRepository.Scope(2L, 30L, true, true));
            dashboard.populateCounts(expired, 4, new DashboardRepository.Scope(2L, 20L, true, true));
            assertEquals(0, unassigned.getMyCourses()); assertEquals(0, unassigned.getPracticeQuestions());
            assertEquals(0, expired.getMyCourses()); assertEquals(0, expired.getPracticeQuestions());
            sql.execute("UPDATE plan_courses SET all_course_questions=false");
            dashboard.populateCounts(studentA, 1, new DashboardRepository.Scope(1L, 10L, true, true));
            assertEquals(0, studentA.getPracticeQuestions());
            sql.execute("INSERT INTO plan_questions VALUES (1,11)");
            dashboard.populateCounts(studentA, 1, new DashboardRepository.Scope(1L, 10L, true, true));
            assertEquals(1, studentA.getPracticeQuestions());
            sql.execute("UPDATE courseassignment SET active_row=false WHERE branch_id=20");
            sql.execute(migration);
            dashboard.populateCounts(studentB, 2, new DashboardRepository.Scope(2L, 20L, true, true));
            assertEquals(0, studentB.getMyCourses());
            try (var rows = sql.executeQuery("SELECT course_id FROM questions WHERE question_id=11")) {
                assertTrue(rows.next()); assertEquals(101L, rows.getLong(1));
            }
        }
    }
}
