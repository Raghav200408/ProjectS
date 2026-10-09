-- ============================================================
-- Adds duration_minutes to exams and mock_exam: how long a student gets
-- once they press Start, set by the admin when the paper is created.
--
-- Time left shown to the student is min(duration_minutes, exam end - now):
-- the duration governs the normal case, but a student who starts late on a
-- scheduled exam still can't run past the exam's own end_date. Mock exams
-- have no end_date (practice any time), so duration_minutes is their only
-- bound.
--
-- Existing rows are backfilled to 60, the value the frontend already
-- hardcoded before this column existed, so no paper's timing changes.
-- ============================================================

ALTER TABLE public.exams
    ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;

UPDATE public.exams
SET duration_minutes = 60
WHERE duration_minutes IS NULL;

ALTER TABLE public.exams
    ALTER COLUMN duration_minutes SET NOT NULL,
    ALTER COLUMN duration_minutes SET DEFAULT 60;

ALTER TABLE public.mock_exam
    ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;

UPDATE public.mock_exam
SET duration_minutes = 60
WHERE duration_minutes IS NULL;

ALTER TABLE public.mock_exam
    ALTER COLUMN duration_minutes SET NOT NULL,
    ALTER COLUMN duration_minutes SET DEFAULT 60;
