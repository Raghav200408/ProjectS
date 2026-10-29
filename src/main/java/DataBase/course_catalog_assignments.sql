-- Separate reusable course catalog records from college and branch availability.
-- Safe to run before the updated backend is deployed; existing course IDs remain unchanged.

ALTER TABLE public.courses
    ALTER COLUMN college_id DROP NOT NULL,
    ALTER COLUMN branch_id DROP NOT NULL;

CREATE TABLE IF NOT EXISTS public.course_assignments (
    assignment_id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES public.courses(course_id) ON DELETE CASCADE,
    college_id BIGINT NOT NULL REFERENCES public.college(college_id) ON DELETE CASCADE,
    branch_id BIGINT NULL REFERENCES public.branch(branch_id) ON DELETE CASCADE,
    active_row BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Preserve every existing course record, even if another course has the same name.
INSERT INTO public.course_assignments (course_id, college_id, branch_id, active_row)
SELECT c.course_id, COALESCE(c.college_id, b.college_id), c.branch_id, COALESCE(c.active_row, TRUE)
FROM public.courses c
LEFT JOIN public.branch b ON b.branch_id = c.branch_id
WHERE COALESCE(c.college_id, b.college_id) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM public.course_assignments a
      WHERE a.course_id = c.course_id
        AND a.college_id = COALESCE(c.college_id, b.college_id)
        AND a.branch_id IS NOT DISTINCT FROM c.branch_id
  );

-- Assignments are now the source of truth for location. Clear legacy pointers
-- so deleting/moving a branch does not move or delete the shared catalog course.
UPDATE public.courses SET branch_id = NULL, college_id = NULL
WHERE branch_id IS NOT NULL OR college_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_course_assignment_branch
    ON public.course_assignments(course_id, college_id, branch_id)
    WHERE branch_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_course_assignment_college_wide
    ON public.course_assignments(course_id, college_id)
    WHERE branch_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_course_assignments_scope
    ON public.course_assignments(college_id, branch_id, active_row);
