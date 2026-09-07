-- Development-only subscription master data.
-- This script reuses existing courses; it never inserts duplicate courses or users.
-- A limit of -1 means unlimited.

INSERT INTO public.subscription_plans (
    name, description, free_trial, active, duration_days,
    practice_question_limit, mock_test_enabled, mock_test_limit,
    exam_enabled, exam_attempt_limit
)
VALUES
    ('Free Trial', '7-day introduction to one course', TRUE, TRUE, 7, 25, FALSE, 0, FALSE, 0),
    ('Basic', 'Entry-level course access', FALSE, TRUE, 30, 100, FALSE, 0, FALSE, 0),
    ('Standard', 'Full practice, mock tests and exams', FALSE, TRUE, 30, 500, TRUE, 10, TRUE, 3),
    ('Premium', 'Unlimited course learning access', FALSE, TRUE, 365, -1, TRUE, -1, TRUE, -1)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    free_trial = EXCLUDED.free_trial,
    active = EXCLUDED.active,
    duration_days = EXCLUDED.duration_days,
    practice_question_limit = EXCLUDED.practice_question_limit,
    mock_test_enabled = EXCLUDED.mock_test_enabled,
    mock_test_limit = EXCLUDED.mock_test_limit,
    exam_enabled = EXCLUDED.exam_enabled,
    exam_attempt_limit = EXCLUDED.exam_attempt_limit,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO public.plan_courses (plan_id, course_id)
SELECT plans.plan_id, courses.course_id
FROM public.subscription_plans plans
JOIN public.courses courses
  ON UPPER(TRIM(courses.name)) IN ('MPC', 'BPC', 'CEC')
WHERE plans.active = TRUE
ON CONFLICT (plan_id, course_id) DO NOTHING;

-- Optional sample subscription: uses the first existing user and MPC course.
-- No row is created if the local database has no users or MPC course.
INSERT INTO public.user_subscriptions (
    user_id, plan_id, course_id, starts_at, expires_at, active
)
SELECT
    users.user_id,
    plans.plan_id,
    courses.course_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + make_interval(days => plans.duration_days),
    TRUE
FROM (
    SELECT users.user_id
    FROM public.users users
    JOIN public.roles roles ON roles.role_id = users.role_id
    WHERE UPPER(roles.role_name) IN ('STUDENT', 'GUEST')
    ORDER BY users.user_id
    LIMIT 1
) users
JOIN public.subscription_plans plans ON plans.name = 'Free Trial'
JOIN (
    SELECT course_id
    FROM public.courses
    WHERE UPPER(TRIM(name)) = 'MPC'
    ORDER BY course_id
    LIMIT 1
) courses ON TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM public.user_subscriptions existing
    WHERE existing.user_id = users.user_id
      AND existing.plan_id = plans.plan_id
      AND existing.course_id = courses.course_id
      AND existing.active = TRUE
);
