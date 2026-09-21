package com.project.ProjectS.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public record Scope(Long collegeId, Long branchId, Long studentId) {
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
                    JOIN roles r ON r.role_id = u.role_id
                    WHERE r.role_name = 'STUDENT'
                      AND COALESCE(u.active_row, true) = true
                """);

        if (scope.collegeId() != null) {
            sql.append("      AND u.college_id = :collegeId\n");
        }
        if (scope.branchId() != null) {
            sql.append("      AND u.branch_id = :branchId\n");
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
                .addValue("studentId", scope.studentId())
                .addValue("courseId", filters.courseId())
                .addValue("subjectId", filters.subjectId())
                .addValue("chapterId", filters.chapterId());
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
     * Units per question: an MCQ is 1; every other question has one unit per
     * ACTIVE question_attributes row. Matching / fill-in-the-blank have no
     * attribute rows, so they contribute 0 until their unit is defined.
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
                attribute_counts AS (
                    SELECT question_id, COUNT(*) AS units
                    FROM question_attributes
                    WHERE COALESCE(active_row, true) = true
                    GROUP BY question_id
                ),
                question_units AS (
                    SELECT q.question_id,
                           CASE WHEN mq.question_id IS NOT NULL THEN 1
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
                    LEFT JOIN question_attributes qa
                           ON qa.question_attribute_id = pr.question_attribute_id
                    WHERE pr.question_attribute_id IS NULL
                       OR COALESCE(qa.active_row, true) = true
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
