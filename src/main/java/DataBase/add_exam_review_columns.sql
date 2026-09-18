-- ============================================================
-- Adds the columns the "Exam Review" screen needs to exam_result and
-- mock_exam_result. Run this once against the Hintmatrix DB before deploying
-- the matching backend build - there is no ddl-auto / Flyway in this project,
-- so schema changes are manual.
--
-- time_taken_seconds   - elapsed seconds on the exam's own countdown,
--                        supplied by the frontend at submit time.
-- maximum_marks        - the marks total computed at submit time, stored so
--                        the review screen never has to re-run scoring
--                        against an exam that may since have changed.
--
-- Both nullable: existing rows (submitted before this change) simply show
-- "—" for these on the review screen.
--
-- What was actually attempted (which account each item was placed into) is
-- NOT stored here - see add_exam_columns_to_answer_tables.sql, which tags
-- the practice flow's own answer_events / question_answers rows with which
-- exam or mock exam they came from.
-- ============================================================

ALTER TABLE public.exam_result
    ADD COLUMN IF NOT EXISTS time_taken_seconds INTEGER,
    ADD COLUMN IF NOT EXISTS maximum_marks DOUBLE PRECISION;

ALTER TABLE public.mock_exam_result
    ADD COLUMN IF NOT EXISTS time_taken_seconds INTEGER,
    ADD COLUMN IF NOT EXISTS maximum_marks DOUBLE PRECISION;

-- If you already ran an earlier version of this migration that added
-- answers_snapshot, it's fine to leave the column - drop it whenever
-- convenient, nothing reads it anymore:
--   ALTER TABLE public.exam_result DROP COLUMN IF EXISTS answers_snapshot;
--   ALTER TABLE public.mock_exam_result DROP COLUMN IF EXISTS answers_snapshot;
