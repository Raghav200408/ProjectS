package com.project.ProjectS.repository;

import com.project.ProjectS.model.DashboardResponseDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** PostgreSQL fixtures use an isolated schema; the entire transaction is rolled back. */
@EnabledIfEnvironmentVariable(named = "DASHBOARD_DB_TEST", matches = "true")
class DashboardRepositoryPostgresTest {
    private Connection connection;
    private JdbcTemplate jdbc;
    private DashboardRepository repository;
    private String schema;

    @BeforeEach void setup() throws Exception {
        Properties props = new Properties();
        try (var input = Files.newInputStream(Path.of("src/main/resources/application.properties"))) { props.load(input); }
        for (String key : List.of("spring.datasource.url", "spring.datasource.username", "spring.datasource.password")) {
            String value = props.getProperty(key);
            if (value.startsWith("${") && value.endsWith("}")) {
                var parts = value.substring(2, value.length() - 1).split(":", 2);
                props.setProperty(key, System.getenv().getOrDefault(parts[0], parts.length == 2 ? parts[1] : ""));
            }
        }
        connection = DriverManager.getConnection(props.getProperty("spring.datasource.url"),
                props.getProperty("spring.datasource.username"), props.getProperty("spring.datasource.password"));
        connection.setAutoCommit(false);
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        schema = "dashboard_test_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE SCHEMA " + schema);
        jdbc.execute("SET LOCAL search_path TO " + schema);
        jdbc.execute("""
            CREATE TABLE roles(role_id bigint PRIMARY KEY, role_name text);
            CREATE TABLE users(user_id bigint PRIMARY KEY, name text, role_id bigint, college_id bigint, branch_id bigint, active_row boolean);
            CREATE TABLE college(college_id bigint PRIMARY KEY, active_row boolean);
            CREATE TABLE branch(branch_id bigint PRIMARY KEY, college_id bigint, active_row boolean);
            CREATE TABLE courses(course_id bigint PRIMARY KEY, name text, college_id bigint, branch_id bigint, active_row boolean);
            CREATE TABLE section(section_id bigint PRIMARY KEY, college_id bigint, branch_id bigint, active_row boolean);
            CREATE TABLE subject(subject_id bigint PRIMARY KEY);
            CREATE TABLE chapters(chapter_id bigint PRIMARY KEY);
            CREATE TABLE topic(topic_id bigint PRIMARY KEY, topic_name text);
            CREATE TABLE questions(question_id bigint PRIMARY KEY, course_id bigint, subject_id bigint, chapter_id bigint, topic_id bigint, active_row boolean);
            CREATE TABLE table_attributes(attribute_id bigint PRIMARY KEY);
            CREATE TABLE question_attributes(question_attribute_id bigint PRIMARY KEY, question_id bigint, attribute_id bigint, active_row boolean);
            CREATE TABLE rule_engines(attribute_id bigint, arithmetic1 text, arithmetic2 text, arithmetic3 text, arithmetic4 text, active_row boolean);
            CREATE TABLE mcq_questions(question_id bigint PRIMARY KEY, active_row boolean);
            CREATE TABLE question_matching_pairs(question_id bigint, pair_id bigint);
            CREATE TABLE question_fill_blank_answers(question_id bigint, blank_number integer);
            CREATE TABLE answer_events(answer_event_id bigint PRIMARY KEY, answer_position integer);
            CREATE TABLE subscription_plans(plan_id bigint PRIMARY KEY, active boolean);
            CREATE TABLE user_subscriptions(subscription_id bigint PRIMARY KEY, user_id bigint, course_id bigint, plan_id bigint, active boolean, starts_at timestamp, expires_at timestamp);
            CREATE TABLE exams(exam_id bigint PRIMARY KEY, exam_name text, active_row boolean);
            CREATE TABLE exam_result(exam_result_id bigint PRIMARY KEY, user_id bigint, exam_id bigint, percentage double precision, created_at timestamp);
            CREATE TABLE mock_exam(mock_exam_id bigint PRIMARY KEY, mock_exam_name text);
            CREATE TABLE mock_exam_result(mock_exam_result_id bigint PRIMARY KEY, user_id bigint, mock_exam_id bigint, percentage double precision, created_at timestamp);
            INSERT INTO roles VALUES (1,'STUDENT'),(2,'SUPER_ADMIN');
            INSERT INTO users VALUES (1,'Alice',1,1,1,true),(2,'Bob',1,1,1,true),(3,'Cara',1,1,1,true),(4,'Other branch',1,1,2,true),(5,'Admin',2,1,1,true);
            INSERT INTO college VALUES (1,true),(2,false);
            INSERT INTO branch VALUES (1,1,true),(2,1,true);
            INSERT INTO section VALUES (1,1,1,true),(2,1,2,true),(3,1,1,false);
            INSERT INTO courses VALUES (1,'Accounting',1,1,true),(2,'Other',1,2,true),(3,'Inactive',1,1,false);
            INSERT INTO subject VALUES (1);
            INSERT INTO chapters VALUES (1);
            INSERT INTO topic VALUES (1,'Journal');
            INSERT INTO questions VALUES (1,1,1,1,1,true),(2,1,1,1,1,true),(3,1,1,1,1,true),(4,1,1,1,1,true),(5,2,1,1,1,true);
            INSERT INTO table_attributes VALUES (10);
            INSERT INTO question_attributes VALUES (100,2,10,true),(101,2,10,true);
            INSERT INTO rule_engines VALUES (10,'a','b',NULL,NULL,true);
            INSERT INTO mcq_questions VALUES (1,true),(5,true);
            INSERT INTO question_matching_pairs VALUES (4,40),(4,41);
            INSERT INTO question_fill_blank_answers VALUES (3,1),(3,1),(3,2);
            INSERT INTO subscription_plans VALUES (1,true);
            INSERT INTO user_subscriptions VALUES (1,1,1,1,true,now()-interval '2 days',NULL),(2,1,2,1,true,now()-interval '5 days',now()-interval '1 day');
            INSERT INTO exams VALUES (1,'Exam A',true),(2,'Exam B',true),(3,'Inactive exam',false);
            INSERT INTO exam_result VALUES (1,1,1,99,now()-interval '2 days'),(2,1,1,50,now()),(3,1,2,90,now()),
                (4,2,1,70,now()),(5,3,1,60,now()),(6,4,1,100,now()),(7,5,1,100,now()),(8,1,3,0,now());
            INSERT INTO mock_exam VALUES (1,'Mock A');
            INSERT INTO mock_exam_result VALUES (1,1,1,80,now());
            """);
        migrate();
        repository = new DashboardRepository(new NamedParameterJdbcTemplate(jdbc));
    }

    private void migrate() throws Exception {
        String sql = Files.readString(Path.of("src/main/java/DataBase/practice_results_complete.sql"))
                .replace("public.", schema + ".")
                .replaceAll("(?m)^BEGIN;\\r?$", "").replaceAll("(?m)^COMMIT;\\r?$", "");
        jdbc.execute(sql);
    }
    private void attempt(long userId, long questionId, String type, Long attribute, Integer position) {
        jdbc.update("""
            INSERT INTO practice_results(user_id, question_id, attribute_id, answer_position, course_id, subject_id,
                chapter_id, topic_id, question_type, is_correct) VALUES (?,?,?,?,1,1,1,1,?,false)
            ON CONFLICT (user_id,question_id,unit_key) DO UPDATE SET attempt_number=practice_results.attempt_number+1, answered_at=now()
            """, userId, questionId, attribute, position, type);
    }
    @AfterEach void cleanup() throws Exception {
        if (connection != null) { connection.rollback(); connection.close(); }
    }

    @Test void metricsCountDistinctPositionsAndOnlyAvailableSubscriptions() {
        attempt(1,2,"ATTRIBUTE",10L,1);
        attempt(1,2,"ATTRIBUTE",10L,2);
        attempt(1,2,"ATTRIBUTE",10L,2);
        attempt(2,1,"MCQ",null,null);
        var response = new DashboardResponseDTO();
        repository.populateCounts(response,1,new DashboardRepository.Scope(1L,1L,false,true));
        assertEquals(1,response.getMyCourses());
        assertEquals(4,response.getPracticeQuestions());
        assertEquals(7,response.getTotalUnits());
        assertEquals(2,response.getAttemptedUnits());
        assertEquals(1,response.getTotalBranches());
        assertEquals(1,response.getTotalSections());
    }
    @Test void rankingUsesLatestExamResultsTiesAndBranchScope() {
        var ranking = repository.ranking(1,new DashboardRepository.Scope(1L,1L,false,true));
        assertEquals(3,ranking.studentCount());
        assertEquals(1L,ranking.currentRank());
        assertEquals(List.of(1L,1L,3L),ranking.entries().stream().map(e -> e.rank()).toList());
        assertEquals(70,ranking.entries().get(0).percentage());
        assertEquals(2,ranking.entries().get(0).examsCompleted());
        assertNull(repository.ranking(5,new DashboardRepository.Scope(1L,1L,false,false)).currentRank());
    }
    @Test void dailyHistoryDeduplicatesQuestionsAndSurvivesLaterAttemptsAndReset() throws Exception {
        LocalDate today = jdbc.queryForObject("SELECT (now() AT TIME ZONE 'Asia/Calcutta')::date", LocalDate.class);
        attempt(1,2,"ATTRIBUTE",10L,1);
        attempt(1,2,"ATTRIBUTE",10L,2);
        attempt(1,2,"ATTRIBUTE",10L,2);
        assertEquals(1,repository.todayQuestions(1,today));
        jdbc.update("INSERT INTO practice_daily_questions VALUES (1,2,?)", today.minusDays(1));
        attempt(1,1,"MCQ",null,null);
        assertEquals(2,repository.todayQuestions(1,today));
        jdbc.update("DELETE FROM answer_events");
        assertEquals(List.of(today,today.minusDays(1)),repository.practiceDays(1,today));
        // Rerunning migrations cannot manufacture a new day for someone who only practised previously.
        jdbc.update("DELETE FROM practice_daily_questions WHERE user_id=1 AND practice_date=?",today);
        jdbc.update("UPDATE practice_results SET answered_at=now()-interval '1 day' WHERE user_id=1");
        jdbc.update("DELETE FROM practice_daily_questions WHERE user_id=1 AND practice_date=?",today);
        migrate();
        assertEquals(0,repository.todayQuestions(1,today));
        assertTrue(repository.practiceDays(1,today).contains(today.minusDays(1)));
    }
    @Test void recentActivitiesArePersonalAndContainNoAchievements() {
        attempt(1,1,"MCQ",null,null);
        attempt(2,2,"ATTRIBUTE",10L,1);
        var activities = repository.recentActivities(1);
        assertTrue(activities.stream().anyMatch(a -> a.type().equals("PRACTICE")));
        assertTrue(activities.stream().anyMatch(a -> a.type().equals("EXAM")));
        assertTrue(activities.stream().anyMatch(a -> a.type().equals("MOCK")));
        assertTrue(activities.stream().anyMatch(a -> a.type().equals("SUBSCRIPTION")));
        assertFalse(activities.stream().anyMatch(a -> a.id().equals("practice:2")));
        for(int i=1;i<activities.size();i++) assertFalse(activities.get(i).occurredAt().isAfter(activities.get(i-1).occurredAt()));
    }
}
