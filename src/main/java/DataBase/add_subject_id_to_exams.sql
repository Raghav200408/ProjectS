-- ============================================================
-- Adds subject_id to the exams table.
--
-- Each exam is now scoped to exactly one subject (the subject its
-- chapters are drawn from). Run this once against the Hintmatrix DB
-- before deploying the matching backend build - there is no
-- ddl-auto / Flyway in this project, so schema changes are manual.
-- ============================================================

BEGIN;

-- 1. Add the column as nullable so existing rows are not rejected.
ALTER TABLE public.exams
    ADD COLUMN IF NOT EXISTS subject_id BIGINT;

-- 2. Backfill every existing exam from the subject of one of its
--    chapters. All chapters on a paper share a subject, so any one
--    of them is correct.
UPDATE public.exams e
SET subject_id = sub.subject_id
FROM (
    SELECT ec.exam_id, MIN(c.subject_id) AS subject_id
    FROM public.exam_chapters ec
    JOIN public.chapters c ON c.chapter_id = ec.chapter_id
    GROUP BY ec.exam_id
) sub
WHERE sub.exam_id = e.exam_id
  AND e.subject_id IS NULL;

-- 3. Sanity check - this should return 0 rows. Any exam listed here
--    has no chapters and must be given a subject_id by hand before
--    step 4 can succeed.
--    SELECT exam_id, exam_name FROM public.exams WHERE subject_id IS NULL;

-- 4. Lock the column down to match the entity (@JoinColumn nullable = false).
ALTER TABLE public.exams
    ALTER COLUMN subject_id SET NOT NULL;

-- 5. Foreign key to subject.
ALTER TABLE public.exams
    ADD CONSTRAINT fk_exams_subject
    FOREIGN KEY (subject_id) REFERENCES public.subject (subject_id);

COMMIT;
