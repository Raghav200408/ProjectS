-- ============================================================
-- Adds exam_questions.marks: how many marks one question is worth on one
-- exam's paper. Set once, when the question is added to the exam
-- (ExamService.addQuestionsToExam), using the same formula the live scorer
-- already uses: 1 for an MCQ (single or multiple choice), or the number of
-- distinct accounting attributes for every other question type.
--
-- This does NOT change live scoring (ExamScoringService.score() is
-- untouched) - it only gives the Performance dashboard's chapter breakdown a
-- stored maximum to read, instead of recomputing it from question_attributes
-- on every request.
--
-- Backfill below applies the exact same formula to every exam_questions row
-- that already exists, so no already-built exam's numbers change.
--
-- mock_exam_questions is intentionally NOT touched.
-- ============================================================

ALTER TABLE public.exam_questions
    ADD COLUMN IF NOT EXISTS marks NUMERIC(10, 2);

-- MCQ (single or multiple choice): 1 mark.
UPDATE public.exam_questions eq
SET marks = 1
FROM public.questions q
         JOIN public.mcq_questions mq ON mq.question_id = q.question_id
WHERE eq.question_id = q.question_id
  AND eq.marks IS NULL;

-- Every other question type: one mark per distinct attribute.
UPDATE public.exam_questions eq
SET marks = attribute_counts.unique_attributes
FROM (
    SELECT question_id, COUNT(DISTINCT attribute_id) AS unique_attributes
    FROM public.question_attributes
    WHERE attribute_id IS NOT NULL
    GROUP BY question_id
) attribute_counts
WHERE eq.question_id = attribute_counts.question_id
  AND eq.marks IS NULL;

-- A question with neither MCQ options nor attributes (shouldn't happen, but
-- the column is NOT NULL below) is worth 0.
UPDATE public.exam_questions
SET marks = 0
WHERE marks IS NULL;

ALTER TABLE public.exam_questions
    ALTER COLUMN marks SET NOT NULL,
    ALTER COLUMN marks SET DEFAULT 1;
