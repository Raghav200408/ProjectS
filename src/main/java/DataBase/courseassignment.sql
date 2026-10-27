-- Review and apply before deploying the course-assignment changes.
-- Existing courses/content/subscriptions keep their IDs; no course data is copied.
BEGIN;
CREATE TABLE IF NOT EXISTS public.courseassignment (
    courseassignment_id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES public.courses(course_id),
    branch_id BIGINT NOT NULL REFERENCES public.branch(branch_id),
    active_row BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_courseassignment_course_branch UNIQUE(course_id, branch_id)
);
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM public.courses c JOIN public.branch b ON b.branch_id = c.branch_id
               WHERE c.college_id IS NOT NULL AND c.college_id <> b.college_id) THEN
        RAISE EXCEPTION 'Resolve inconsistent legacy course/branch/college links before migrating';
    END IF;
END $$;
INSERT INTO public.courseassignment(course_id, branch_id)
SELECT course_id, branch_id FROM public.courses WHERE branch_id IS NOT NULL
ON CONFLICT(course_id, branch_id) DO NOTHING;
-- Legacy origin metadata is retained; new shared courses need no institution owner.
ALTER TABLE public.courses ALTER COLUMN college_id DROP NOT NULL;
ALTER TABLE public.courses ALTER COLUMN branch_id DROP NOT NULL;
-- Old course-wide section uniqueness would prevent two branches using Section A.
DO $$ DECLARE constraint_row RECORD; BEGIN
    FOR constraint_row IN
        SELECT c.conname FROM pg_constraint c
        WHERE c.conrelid = 'public.section'::regclass AND c.contype = 'u'
          AND (SELECT array_agg(a.attname::text ORDER BY a.attname)
               FROM unnest(c.conkey) k JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = k)
              = ARRAY['course_id', 'section_name']::text[]
    LOOP EXECUTE format('ALTER TABLE public.section DROP CONSTRAINT %I', constraint_row.conname); END LOOP;
END $$;
CREATE UNIQUE INDEX IF NOT EXISTS uq_section_branch_course_name ON public.section(branch_id, course_id, section_name);
-- Deduplicate question usage across effects/retries for each personal subscription.
CREATE TABLE IF NOT EXISTS public.subscription_question_usage (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES public.user_subscriptions(subscription_id),
    question_id BIGINT NOT NULL REFERENCES public.questions(question_id),
    CONSTRAINT uq_subscription_question_usage UNIQUE(subscription_id, question_id)
);
COMMIT;
