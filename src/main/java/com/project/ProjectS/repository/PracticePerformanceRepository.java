package com.project.ProjectS.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

/**
 * Aggregation queries for the practice performance screens.
 *
 * Everything is worked out in PostgreSQL and comes back as one row per
 * course / subject / chapter / topic - nothing is loaded into Java to be
 * summed. Attempted / correct come ONLY from practice_results (the current
 * state, one row per unit); answer_events is never counted, since it holds
 * every retry.
 */
@Repository
public class PracticePerformanceRepository {

    public enum Level {
        // entity table, id column, name column, then which columns of the
        // entity table hold its course / subject / chapter (null = the level
        // has no such column). A level's own id counts as its own filter.
        COURSE("courses", "course_id", "name",
                "course_id", null, null, "course_id",
                null, null, null, null),
        SUBJECT("subject", "subject_id", "subject_name",
                "course_id", "subject_id", null, "subject_id",
                "courses", "course_id", "course_id", "name"),
        CHAPTER("chapters", "chapter_id", "name",
                "course_id", "subject_id", "chapter_id", "chapter_id",
                "subject", "subject_id", "subject_id", "subject_name"),
        TOPIC("topic", "topic_id", "topic_name",
                "course_id", "subject_id", "chapter_id", "topic_id",
                "chapters", "chapter_id", "chapter_id", "name");

        final String table;
        final String idColumn;
        final String nameColumn;
        final String courseColumn;
        final String subjectColumn;
        final String chapterColumn;
        final String questionColumn;
        // The level above, only to show its name next to a row (topic names
        // repeat across chapters). Null for COURSE.
        final String parentTable;
        final String parentFkColumn;
        final String parentIdColumn;
        final String parentNameColumn;

        Level(String table, String idColumn, String nameColumn,
              String courseColumn, String subjectColumn,
              String chapterColumn, String questionColumn,
              String parentTable, String parentFkColumn,
              String parentIdColumn, String parentNameColumn) {
            this.table = table;
            this.idColumn = idColumn;
            this.nameColumn = nameColumn;
            this.courseColumn = courseColumn;
            this.subjectColumn = subjectColumn;
            this.chapterColumn = chapterColumn;
            this.questionColumn = questionColumn;
            this.parentTable = parentTable;
            this.parentFkColumn = parentFkColumn;
            this.parentIdColumn = parentIdColumn;
            this.parentNameColumn = parentNameColumn;
        }
    }

    // Which students the numbers cover. Any field left null isn't filtered on.
    public record Scope(
            Long collegeId,
            Long branchId,
            Long sectionId,
            Long studentId,
            boolean includeAllRoles) {
    }

    // Optional narrowing by hierarchy. Each one only applies to levels that
    // have that column (a subject has no chapter, so chapterId is ignored
    // for the SUBJECT level).
    public record Filters(Long courseId, Long subjectId, Long chapterId) {
        public static final Filters NONE = new Filters(null, null, null);
    }

    public record Row(
            long id,
            String name,
            // Name of the level above (a topic's chapter, a chapter's
            // subject, ...); null for courses.
            String parentName,
            long totalUnits,
            long attemptedUnits,
            long correctUnits) {
    }

    public record TimelineRow(LocalDate date, long answerCount, long correctCount) {
    }

    public record StudentSupportRow(
            long userId, String studentName, Long studentCode, String sectionName,
            long totalUnits, long attemptedUnits, long correctUnits, java.math.BigDecimal accuracyPercentage) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public PracticePerformanceRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Active students in scope. Counted from users/roles - never from
    // practice_results - so a student who hasn't attempted anything is still
    // a student.
    private static String studentScopeCte(Scope scope) {

        StringBuilder sql = new StringBuilder("""
                student_scope AS (
                    SELECT u.user_id
                    FROM users u
                    WHERE COALESCE(u.active_row, true) = true
                """);

        // Reporting views aggregate students only. The dedicated "mine"
        // endpoint sets includeAllRoles so administrators and guests can see
        // their own practice progress without entering student reports.
        if (!scope.includeAllRoles()) {
            sql.append("      AND EXISTS (SELECT 1 FROM roles r "
                    + "WHERE r.role_id = u.role_id AND r.role_name = 'STUDENT')\n");
        }

        if (scope.collegeId() != null) {
            sql.append("      AND u.college_id = :collegeId\n");
        }
        if (scope.branchId() != null) {
            sql.append("      AND u.branch_id = :branchId\n");
        }
        if (scope.sectionId() != null) {
            sql.append("      AND u.section_id = :sectionId\n");
        }
        if (scope.studentId() != null) {
            sql.append("      AND u.user_id = :studentId\n");
        }

        return sql.append("                )").toString();
    }

    private static MapSqlParameterSource params(Scope scope, Filters filters) {

        return new MapSqlParameterSource()
                .addValue("collegeId", scope.collegeId())
                .addValue("branchId", scope.branchId())
                .addValue("sectionId", scope.sectionId())
                .addValue("studentId", scope.studentId())
                .addValue("courseId", filters.courseId())
                .addValue("subjectId", filters.subjectId())
                .addValue("chapterId", filters.chapterId());
    }

    public List<StudentSupportRow> findStudentsNeedingSupport(Filters filters, Scope scope) {
        StringBuilder sql = new StringBuilder("WITH ")
                .append(studentScopeCte(scope))
                .append("""
                        , attribute_positions AS (
                            SELECT DISTINCT qa.question_id, qa.attribute_id, pos.answer_position
                            FROM question_attributes qa
                            JOIN rule_engines re ON re.attribute_id = qa.attribute_id AND re.active_row = true
                            CROSS JOIN LATERAL (VALUES
                                (1, re.arithmetic1), (2, re.arithmetic2),
                                (3, re.arithmetic3), (4, re.arithmetic4)
                            ) pos(answer_position, arithmetic)
                            WHERE COALESCE(qa.active_row, true) = true AND pos.arithmetic IS NOT NULL
                        ),
                        attribute_counts AS (
                            SELECT question_id, COUNT(*) AS units FROM attribute_positions GROUP BY question_id
                        ),
                        question_units AS (
                            SELECT q.question_id,
                                   CASE WHEN mq.question_id IS NOT NULL THEN 1
                                        WHEN EXISTS (SELECT 1 FROM question_matching_pairs mp WHERE mp.question_id = q.question_id)
                                        THEN (SELECT COUNT(*) FROM question_matching_pairs mp WHERE mp.question_id = q.question_id)
                                        WHEN EXISTS (SELECT 1 FROM question_fill_blank_answers fb WHERE fb.question_id = q.question_id)
                                        THEN (SELECT COUNT(DISTINCT COALESCE(fb.blank_number, 1)) FROM question_fill_blank_answers fb WHERE fb.question_id = q.question_id)
                                        ELSE COALESCE(ac.units, 0) END AS units
                            FROM questions q
                            LEFT JOIN mcq_questions mq ON mq.question_id = q.question_id AND mq.active_row = true
                            LEFT JOIN attribute_counts ac ON ac.question_id = q.question_id
                            WHERE COALESCE(q.active_row, true) = true
                        """);
        if (filters.courseId() != null) sql.append(" AND q.course_id = :courseId\n");
        if (filters.subjectId() != null) sql.append(" AND q.subject_id = :subjectId\n");
        if (filters.chapterId() != null) sql.append(" AND q.chapter_id = :chapterId\n");
        sql.append("""
                        ),
                        totals AS (
                            SELECT COALESCE(SUM(units), 0) AS total_units
                            FROM question_units WHERE units > 0
                        ),
                        student_results AS (
                            SELECT pr.user_id,
                                   COUNT(*) AS attempted_units,
                                   COUNT(*) FILTER (WHERE pr.is_correct IS TRUE) AS correct_units
                            FROM practice_results pr
                            JOIN student_scope ss ON ss.user_id = pr.user_id
                            JOIN question_units qu ON qu.question_id = pr.question_id AND qu.units > 0
                            WHERE 1 = 1
                        """);
        if (filters.courseId() != null) sql.append(" AND pr.course_id = :courseId\n");
        if (filters.subjectId() != null) sql.append(" AND pr.subject_id = :subjectId\n");
        if (filters.chapterId() != null) sql.append(" AND pr.chapter_id = :chapterId\n");
        sql.append("""
                              AND (pr.question_type <> 'ATTRIBUTE' OR EXISTS (
                                  SELECT 1 FROM attribute_positions ap
                                  WHERE ap.question_id = pr.question_id AND ap.attribute_id = pr.attribute_id
                                    AND ap.answer_position = pr.answer_position))
                              AND (pr.question_type NOT IN ('FILL_BLANK', 'MATCHING')
                                OR (pr.question_type = 'FILL_BLANK' AND EXISTS (
                                    SELECT 1 FROM question_fill_blank_answers fb
                                    WHERE fb.question_id = pr.question_id
                                      AND COALESCE(fb.blank_number, 1) = pr.answer_position))
                                OR (pr.question_type = 'MATCHING' AND EXISTS (
                                    SELECT 1 FROM question_matching_pairs mp
                                    WHERE mp.question_id = pr.question_id AND mp.pair_id = pr.answer_position)))
                            GROUP BY pr.user_id
                        )
                        SELECT u.user_id, u.name AS student_name, u.student_id AS student_code,
                               s.section_name, totals.total_units,
                               COALESCE(sr.attempted_units, 0) AS attempted_units,
                               COALESCE(sr.correct_units, 0) AS correct_units,
                               CASE WHEN COALESCE(sr.attempted_units, 0) = 0 THEN NULL
                                    ELSE ROUND(sr.correct_units * 100.0 / sr.attempted_units, 2) END AS accuracy_percentage
                        FROM student_scope ss
                        JOIN users u ON u.user_id = ss.user_id
                        LEFT JOIN section s ON s.section_id = u.section_id
                        CROSS JOIN totals
                        LEFT JOIN student_results sr ON sr.user_id = u.user_id
                        WHERE COALESCE(sr.attempted_units, 0) = 0
                           OR sr.correct_units * 100.0 / NULLIF(sr.attempted_units, 0) < 60
                        ORDER BY CASE WHEN COALESCE(sr.attempted_units, 0) = 0 THEN 0 ELSE 1 END,
                                 accuracy_percentage NULLS FIRST, u.name
                        LIMIT 10
                        """);
        return jdbc.query(sql.toString(), params(scope, filters),
                (rs, rowNum) -> new StudentSupportRow(
                        rs.getLong("user_id"),
                        rs.getString("student_name"),
                        rs.getObject("student_code", Long.class),
                        rs.getString("section_name"),
                        rs.getLong("total_units"),
                        rs.getLong("attempted_units"),
                        rs.getLong("correct_units"),
                        rs.getBigDecimal("accuracy_percentage")));
    }

    public List<TimelineRow> findTimeline(Filters filters, Scope scope) {
        StringBuilder sql = new StringBuilder("WITH ")
                .append(studentScopeCte(scope))
                .append("""
                        , days AS (
                            SELECT generate_series(CURRENT_DATE - 13, CURRENT_DATE, INTERVAL '1 day')::date AS activity_date
                        ),
                        daily AS (
                            SELECT ae.created_at::date AS activity_date,
                                   COUNT(*) AS answer_count,
                                   COUNT(*) FILTER (WHERE ae.is_correct IS TRUE) AS correct_count
                            FROM answer_events ae
                            JOIN student_scope ss ON ss.user_id = ae.user_id
                            JOIN questions q ON q.question_id = ae.question_id
                            WHERE ae.event_type = 'ANSWER'
                              AND ae.exam_id IS NULL
                              AND ae.mock_exam_id IS NULL
                              AND COALESCE(ae.active_row, true) = true
                              AND ae.created_at >= CURRENT_DATE - INTERVAL '13 days'
                              AND ae.created_at < CURRENT_DATE + INTERVAL '1 day'
                        """);
        if (filters.courseId() != null) sql.append(" AND q.course_id = :courseId\n");
        if (filters.subjectId() != null) sql.append(" AND q.subject_id = :subjectId\n");
        if (filters.chapterId() != null) sql.append(" AND q.chapter_id = :chapterId\n");
        sql.append("""
                            GROUP BY ae.created_at::date
                        )
                        SELECT days.activity_date,
                               COALESCE(daily.answer_count, 0) AS answer_count,
                               COALESCE(daily.correct_count, 0) AS correct_count
                        FROM days
                        LEFT JOIN daily USING (activity_date)
                        ORDER BY days.activity_date
                        """);
        return jdbc.query(sql.toString(), params(scope, filters),
                (rs, rowNum) -> new TimelineRow(
                        rs.getDate("activity_date").toLocalDate(),
                        rs.getLong("answer_count"),
                        rs.getLong("correct_count")));
    }

    public long countStudents(Scope scope) {

        String sql = "WITH " + studentScopeCte(scope)
                + " SELECT COUNT(*) FROM student_scope";

        Long count = jdbc.queryForObject(sql, params(scope, Filters.NONE), Long.class);

        return count == null ? 0 : count;
    }

    /**
     * One row per active entity at the level (optionally narrowed by
     * course / subject / chapter), with:
     *
     *   totalUnits     = units per student at that level x students in scope
     *   attemptedUnits = current practice_results rows of those students
     *   correctUnits   = ...of which is_correct
     *
     * Units per question: an MCQ is 1; attribute questions have one unit per
     * active rule answer position on each active attribute. Matching counts source pairs and
     * fill-in-the-blank counts distinct blank positions, not accepted answers.
     *
     * The totals and the attempts are aggregated separately and joined by
     * level id, so a question with 5 attributes and 3 attempts gives
     * total 5 / attempted 3 - the row counts never multiply each other.
     */
    public List<Row> findMetrics(Level level, Filters filters, Scope scope) {

        String column = level.questionColumn;

        StringBuilder sql = new StringBuilder("WITH ");

        sql.append(studentScopeCte(scope)).append(",\n");

        sql.append("""
                attribute_positions AS (
                    SELECT DISTINCT qa.question_id, qa.attribute_id, pos.answer_position
                    FROM question_attributes qa
                    JOIN rule_engines re ON re.attribute_id = qa.attribute_id AND re.active_row = true
                    CROSS JOIN LATERAL (VALUES
                        (1, re.arithmetic1), (2, re.arithmetic2),
                        (3, re.arithmetic3), (4, re.arithmetic4)
                    ) pos(answer_position, arithmetic)
                    WHERE COALESCE(qa.active_row, true) = true AND pos.arithmetic IS NOT NULL
                ),
                attribute_counts AS (
                    SELECT question_id, COUNT(*) AS units FROM attribute_positions GROUP BY question_id
                ),
                question_units AS (
                    SELECT q.question_id,
                           CASE WHEN mq.question_id IS NOT NULL THEN 1
                                WHEN EXISTS (SELECT 1 FROM question_matching_pairs mp WHERE mp.question_id = q.question_id)
                                THEN (SELECT COUNT(*) FROM question_matching_pairs mp WHERE mp.question_id = q.question_id)
                                WHEN EXISTS (SELECT 1 FROM question_fill_blank_answers fb WHERE fb.question_id = q.question_id)
                                THEN (SELECT COUNT(DISTINCT COALESCE(fb.blank_number, 1)) FROM question_fill_blank_answers fb WHERE fb.question_id = q.question_id)
                                ELSE COALESCE(ac.units, 0) END AS units,
                """);
        sql.append("               q.").append(column).append(" AS level_id\n");
        sql.append("""
                    FROM questions q
                    LEFT JOIN mcq_questions mq
                           ON mq.question_id = q.question_id
                          AND mq.active_row = true
                    LEFT JOIN attribute_counts ac
                           ON ac.question_id = q.question_id
                    WHERE COALESCE(q.active_row, true) = true
                ),
                totals AS (
                    SELECT level_id, SUM(units) AS units
                    FROM question_units
                    GROUP BY level_id
                ),
                attempts AS (
                """);
        sql.append("    SELECT pr.").append(column).append(" AS level_id,\n");
        sql.append("""
                           COUNT(*) AS attempted,
                           COUNT(*) FILTER (WHERE pr.is_correct) AS correct
                    FROM practice_results pr
                    JOIN student_scope ss ON ss.user_id = pr.user_id
                    JOIN question_units qu
                           ON qu.question_id = pr.question_id
                          AND qu.units > 0
                    WHERE (pr.question_type <> 'ATTRIBUTE' OR EXISTS (
                          SELECT 1 FROM attribute_positions ap
                          WHERE ap.question_id = pr.question_id AND ap.attribute_id = pr.attribute_id
                            AND ap.answer_position = pr.answer_position))
                      AND (pr.question_type NOT IN ('FILL_BLANK', 'MATCHING')
                        OR (pr.question_type = 'FILL_BLANK' AND EXISTS (
                            SELECT 1 FROM question_fill_blank_answers fb
                            WHERE fb.question_id = pr.question_id
                              AND COALESCE(fb.blank_number, 1) = pr.answer_position))
                        OR (pr.question_type = 'MATCHING' AND EXISTS (
                            SELECT 1 FROM question_matching_pairs mp
                            WHERE mp.question_id = pr.question_id AND mp.pair_id = pr.answer_position)))
                """);
        sql.append("    GROUP BY pr.").append(column).append("\n)\n");

        sql.append("SELECT e.").append(level.idColumn).append(" AS id,\n");
        sql.append("       e.").append(level.nameColumn).append(" AS name,\n");
        sql.append(level.parentTable == null
                ? "       NULL AS parent_name,\n"
                : "       p." + level.parentNameColumn + " AS parent_name,\n");
        sql.append("""
                       COALESCE(t.units, 0)
                           * (SELECT COUNT(*) FROM student_scope) AS total_units,
                       COALESCE(a.attempted, 0) AS attempted_units,
                       COALESCE(a.correct, 0) AS correct_units
                """);
        sql.append("FROM ").append(level.table).append(" e\n");
        if (level.parentTable != null) {
            sql.append("LEFT JOIN ").append(level.parentTable)
                    .append(" p ON p.").append(level.parentIdColumn)
                    .append(" = e.").append(level.parentFkColumn).append("\n");
        }
        sql.append("LEFT JOIN totals t ON t.level_id = e.")
                .append(level.idColumn).append("\n");
        sql.append("LEFT JOIN attempts a ON a.level_id = e.")
                .append(level.idColumn).append("\n");
        sql.append("WHERE COALESCE(e.active_row, true) = true\n");

        if (filters.courseId() != null && level.courseColumn != null) {
            sql.append("  AND e.").append(level.courseColumn)
                    .append(" = :courseId\n");
        }
        if (filters.subjectId() != null && level.subjectColumn != null) {
            sql.append("  AND e.").append(level.subjectColumn)
                    .append(" = :subjectId\n");
        }
        if (filters.chapterId() != null && level.chapterColumn != null) {
            sql.append("  AND e.").append(level.chapterColumn)
                    .append(" = :chapterId\n");
        }

        sql.append("ORDER BY e.").append(level.nameColumn).append(", e.")
                .append(level.idColumn);

        return jdbc.query(sql.toString(), params(scope, filters),
                (rs, rowNum) -> new Row(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("parent_name"),
                        rs.getLong("total_units"),
                        rs.getLong("attempted_units"),
                        rs.getLong("correct_units")));
    }
}
