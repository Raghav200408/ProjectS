-- ============================================================
-- Tags answer_events with which exam (or mock exam) attempt a row came
-- from. Run this once against the Hintmatrix DB before deploying the
-- matching backend build - there is no ddl-auto / Flyway in this project,
-- so schema changes are manual.
--
-- question_answers is NOT touched here - only answer_events. That table
-- stays exactly as it is.
--
-- Both columns are nullable and mutually exclusive per row (a practice-flow
-- row has neither set; an exam-flow row has exam_id set; a mock-exam-flow
-- row has mock_exam_id set). getOverallMarks() (the practice "Total Score"
-- widget) only sums rows where both are null, so exam/mock-exam attempts
-- never bleed into it.
--
-- persistAnswerInfo (ExamScoringService) deletes every row for
-- (user, exam_id) or (user, mock_exam_id) before writing a fresh
-- submission's rows, so retaking the same paper replaces its prior
-- attempt instead of accumulating rows alongside it.
-- ============================================================

ALTER TABLE public.answer_events
    ADD COLUMN IF NOT EXISTS exam_id BIGINT,
    ADD COLUMN IF NOT EXISTS mock_exam_id BIGINT;

-- Safe to re-run: skips each constraint if it's already there (e.g. from an
-- earlier version of this migration).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_answer_events_exam'
    ) THEN
        ALTER TABLE public.answer_events
            ADD CONSTRAINT fk_answer_events_exam
                FOREIGN KEY (exam_id) REFERENCES public.exams (exam_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_answer_events_mock_exam'
    ) THEN
        ALTER TABLE public.answer_events
            ADD CONSTRAINT fk_answer_events_mock_exam
                FOREIGN KEY (mock_exam_id) REFERENCES public.mock_exam (mock_exam_id);
    END IF;
END $$;
