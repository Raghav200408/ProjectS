-- Apply alongside FinalAccountsDragDrop.sql on deployment. No existing question
-- types, questions or accounting rules are renamed or reassigned.
BEGIN;
ALTER TABLE public.question_attributes ADD COLUMN IF NOT EXISTS is_adjustment boolean DEFAULT false;
INSERT INTO public.question_type (question_type, created_at)
SELECT 'dragAndDropWithAdj', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM public.question_type
  WHERE regexp_replace(lower(question_type), '[^a-z0-9]', '', 'g') = 'draganddropwithadj'
);
COMMIT;
