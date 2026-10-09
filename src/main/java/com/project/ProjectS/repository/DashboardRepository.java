package com.project.ProjectS.repository;

import com.project.ProjectS.model.DashboardResponseDTO;
import com.project.ProjectS.model.DashboardResponseDTO.Activity;
import com.project.ProjectS.model.DashboardResponseDTO.LeaderboardEntry;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class DashboardRepository {
    public static final ZoneId PRACTICE_ZONE = ZoneId.of("Asia/Calcutta");
    public record Scope(Long collegeId, Long branchId, boolean selfOnly, boolean subscribedOnly) {}
    public record Ranking(List<LeaderboardEntry> entries, Long currentRank, long studentCount) {}
    private final NamedParameterJdbcTemplate jdbc;

    public DashboardRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private MapSqlParameterSource params(long userId, Scope scope) {
        return new MapSqlParameterSource("userId", userId)
                .addValue("collegeId", scope.collegeId()).addValue("branchId", scope.branchId())
                .addValue("selfOnly", scope.selfOnly()).addValue("subscribedOnly", scope.subscribedOnly());
    }

    public void populateCounts(DashboardResponseDTO result, long userId, Scope scope) {
        var p = params(userId, scope);
        var counts = jdbc.queryForMap("""
                SELECT
                  (SELECT COUNT(*) FROM college c WHERE COALESCE(c.active_row, true)
                    AND (CAST(:collegeId AS bigint) IS NULL OR c.college_id = :collegeId)
                    AND (CAST(:branchId AS bigint) IS NULL OR EXISTS (
                      SELECT 1 FROM branch b WHERE b.branch_id = :branchId AND b.college_id = c.college_id))
                    AND NOT :selfOnly) AS colleges,
                  (SELECT COUNT(*) FROM branch b WHERE COALESCE(b.active_row, true)
                    AND (CAST(:collegeId AS bigint) IS NULL OR b.college_id = :collegeId)
                    AND (CAST(:branchId AS bigint) IS NULL OR b.branch_id = :branchId)
                    AND NOT :selfOnly) AS branches,
                  (SELECT COUNT(*) FROM section s WHERE COALESCE(s.active_row, true)
                    AND (CAST(:collegeId AS bigint) IS NULL OR s.college_id = :collegeId)
                    AND (CAST(:branchId AS bigint) IS NULL OR s.branch_id = :branchId)
                    AND NOT :selfOnly) AS sections,
                  (SELECT COUNT(*) FROM courses c WHERE COALESCE(c.active_row, true)
                    AND (CAST(:collegeId AS bigint) IS NULL OR c.college_id = :collegeId)
                    AND (CAST(:branchId AS bigint) IS NULL OR c.branch_id = :branchId)
                    AND NOT :selfOnly) AS courses,
                  (SELECT COUNT(DISTINCT c.course_id) FROM user_subscriptions us
                    JOIN courses c ON c.course_id = us.course_id AND COALESCE(c.active_row, true)
                    JOIN subscription_plans sp ON sp.plan_id = us.plan_id AND sp.active
                    WHERE us.user_id = :userId AND us.active AND us.starts_at <= (now() AT TIME ZONE 'Asia/Calcutta')
                      AND (us.expires_at IS NULL OR us.expires_at > (now() AT TIME ZONE 'Asia/Calcutta'))) AS my_courses
                """, p);
        result.setTotalColleges(((Number) counts.get("colleges")).longValue());
        result.setTotalBranches(((Number) counts.get("branches")).longValue());
        result.setTotalSections(((Number) counts.get("sections")).longValue());
        result.setTotalCourses(((Number) counts.get("courses")).longValue());
        result.setMyCourses(((Number) counts.get("my_courses")).longValue());

        // Numerator and denominator share the same active unit definitions.
        var practice = jdbc.queryForMap("""
                WITH available_questions AS (
                  SELECT q.* FROM questions q JOIN courses c ON c.course_id = q.course_id
                  WHERE COALESCE(q.active_row, true) AND COALESCE(c.active_row, true)
                    AND ((:subscribedOnly AND EXISTS (
                      SELECT 1 FROM user_subscriptions us
                      JOIN subscription_plans sp ON sp.plan_id = us.plan_id AND sp.active
                      WHERE us.user_id = :userId AND us.course_id = q.course_id AND us.active
                        AND us.starts_at <= (now() AT TIME ZONE 'Asia/Calcutta')
                        AND (us.expires_at IS NULL OR us.expires_at > (now() AT TIME ZONE 'Asia/Calcutta'))))
                      OR (NOT :subscribedOnly AND NOT :selfOnly
                        AND (CAST(:collegeId AS bigint) IS NULL OR c.college_id = :collegeId)
                        AND (CAST(:branchId AS bigint) IS NULL OR c.branch_id = :branchId)))
                ), question_kinds AS (
                  SELECT q.question_id, CASE
                    WHEN EXISTS (SELECT 1 FROM mcq_questions m WHERE m.question_id = q.question_id AND m.active_row) THEN 'MCQ'
                    WHEN EXISTS (SELECT 1 FROM question_matching_pairs m WHERE m.question_id = q.question_id) THEN 'MATCHING'
                    WHEN EXISTS (SELECT 1 FROM question_fill_blank_answers f WHERE f.question_id = q.question_id) THEN 'FILL_BLANK'
                    ELSE 'ATTRIBUTE' END AS kind FROM available_questions q
                ), units AS (
                  SELECT question_id, 'MCQ' AS unit_key FROM question_kinds WHERE kind = 'MCQ'
                  UNION
                  SELECT q.question_id, 'MATCHING:' || mp.pair_id::text
                    FROM question_kinds q JOIN question_matching_pairs mp USING (question_id) WHERE q.kind = 'MATCHING'
                  UNION
                  SELECT q.question_id, 'FILL_BLANK:' || COALESCE(fb.blank_number, 1)::text
                    FROM question_kinds q JOIN question_fill_blank_answers fb USING (question_id) WHERE q.kind = 'FILL_BLANK'
                  UNION
                  SELECT q.question_id, 'ATTRIBUTE:' || qa.attribute_id::text || ':' || pos.position::text
                    FROM question_kinds q JOIN question_attributes qa USING (question_id)
                    JOIN rule_engines re ON re.attribute_id = qa.attribute_id AND re.active_row
                    CROSS JOIN LATERAL (VALUES (1, re.arithmetic1), (2, re.arithmetic2),
                        (3, re.arithmetic3), (4, re.arithmetic4)) pos(position, arithmetic)
                    WHERE q.kind = 'ATTRIBUTE' AND COALESCE(qa.active_row, true) AND pos.arithmetic IS NOT NULL
                )
                SELECT COUNT(DISTINCT u.question_id) AS questions, COUNT(*) AS total,
                    COUNT(pr.practice_result_id) AS attempted
                FROM units u LEFT JOIN practice_results pr
                  ON pr.question_id = u.question_id AND pr.unit_key = u.unit_key AND pr.user_id = :userId
                """, p);
        result.setPracticeQuestions(((Number) practice.get("questions")).longValue());
        result.setTotalUnits(((Number) practice.get("total")).longValue());
        result.setAttemptedUnits(((Number) practice.get("attempted")).longValue());
    }

    public Ranking ranking(long userId, Scope scope) {
        // A retake replaces that exam's earlier percentage; exams get equal weight.
        var rows = jdbc.query("""
                WITH latest AS (
                  SELECT DISTINCT ON (er.user_id, er.exam_id) er.user_id, er.exam_id, er.percentage
                  FROM exam_result er JOIN users u ON u.user_id = er.user_id
                  JOIN roles r ON r.role_id = u.role_id
                  JOIN exams e ON e.exam_id = er.exam_id AND COALESCE(e.active_row, true)
                  WHERE r.role_name = 'STUDENT' AND COALESCE(u.active_row, true)
                    AND (CAST(:collegeId AS bigint) IS NULL OR u.college_id = :collegeId)
                    AND (CAST(:branchId AS bigint) IS NULL OR u.branch_id = :branchId)
                    AND (NOT :selfOnly OR u.user_id = :userId)
                  ORDER BY er.user_id, er.exam_id, er.created_at DESC NULLS LAST, er.exam_result_id DESC
                ), averages AS (
                  SELECT user_id, AVG(percentage)::numeric AS percentage, COUNT(*) AS exams
                  FROM latest GROUP BY user_id
                ), ranked AS (
                  SELECT a.*, RANK() OVER (ORDER BY percentage DESC) AS position,
                    COUNT(*) OVER () AS students FROM averages a
                )
                SELECT r.*, u.name FROM ranked r JOIN users u ON u.user_id = r.user_id
                WHERE r.user_id = :userId OR r.user_id IN (
                    SELECT user_id FROM ranked ORDER BY position, user_id LIMIT 5)
                ORDER BY position, u.name, r.user_id
                """, params(userId, scope), (rs, index) -> new RankedRow(
                new LeaderboardEntry(rs.getLong("user_id"), rs.getString("name"), rs.getLong("position"),
                    rs.getDouble("percentage"), rs.getLong("exams"), rs.getLong("user_id") == userId), rs.getLong("students")));
        Long rank = rows.stream().map(RankedRow::entry).filter(LeaderboardEntry::current)
                .map(LeaderboardEntry::rank).findFirst().orElse(null);
        return new Ranking(rows.stream().map(RankedRow::entry).toList(), rank,
                rows.isEmpty() ? 0 : rows.get(0).students());
    }
    private record RankedRow(LeaderboardEntry entry, long students) {}

    public List<LocalDate> practiceDays(long userId, LocalDate today) {
        return jdbc.query("""
                SELECT DISTINCT practice_date FROM practice_daily_questions
                WHERE user_id = :userId AND practice_date <= :today ORDER BY practice_date DESC
                """, new MapSqlParameterSource("userId", userId).addValue("today", today),
                (rs, index) -> rs.getObject("practice_date", LocalDate.class));
    }

    public long todayQuestions(long userId, LocalDate today) {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM practice_daily_questions WHERE user_id = :userId AND practice_date = :today
                """, new MapSqlParameterSource("userId", userId).addValue("today", today), Long.class);
    }

    public List<Activity> recentActivities(long userId) {
        return jdbc.query("""
                SELECT * FROM (
                  SELECT 'practice:' || pr.question_id AS id, 'PRACTICE' AS type,
                    'Practiced question #' || pr.question_id AS title,
                    COALESCE(t.topic_name, 'Practice') || ' · ' || COUNT(*) || ' units · '
                      || COUNT(*) FILTER (WHERE pr.is_correct) || ' correct' AS detail,
                    MAX(pr.answered_at) AS occurred_at
                  FROM practice_results pr JOIN questions q ON q.question_id = pr.question_id
                    LEFT JOIN topic t ON t.topic_id = q.topic_id WHERE pr.user_id = :userId GROUP BY pr.question_id, t.topic_name
                  UNION ALL
                  SELECT 'exam:' || er.exam_result_id, 'EXAM', 'Completed exam: ' || e.exam_name,
                    ROUND(er.percentage::numeric, 1)::text || '%', er.created_at
                    FROM exam_result er JOIN exams e ON e.exam_id = er.exam_id WHERE er.user_id = :userId
                  UNION ALL
                  SELECT 'mock:' || mr.mock_exam_result_id, 'MOCK', 'Completed mock test: ' || m.mock_exam_name,
                    ROUND(mr.percentage::numeric, 1)::text || '%', mr.created_at
                    FROM mock_exam_result mr JOIN mock_exam m ON m.mock_exam_id = mr.mock_exam_id WHERE mr.user_id = :userId
                  UNION ALL
                  SELECT 'subscription:' || us.subscription_id, 'SUBSCRIPTION', 'Started subscription: ' || c.name,
                    'Course subscription', us.starts_at FROM user_subscriptions us JOIN courses c ON c.course_id = us.course_id
                    WHERE us.user_id = :userId AND us.starts_at <= (now() AT TIME ZONE 'Asia/Calcutta')
                ) activity WHERE occurred_at IS NOT NULL ORDER BY occurred_at DESC, id DESC LIMIT 12
                """, new MapSqlParameterSource("userId", userId), (rs, index) -> new Activity(
                rs.getString("id"), rs.getString("type"), rs.getString("title"), rs.getString("detail"),
                rs.getObject("occurred_at", LocalDateTime.class).atZone(PRACTICE_ZONE).toOffsetDateTime()));
    }
}
