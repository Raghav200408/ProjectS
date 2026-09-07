-- Course-level subscription schema for the existing courses and users tables.
-- Run after the base ProjectS schema.

CREATE TABLE IF NOT EXISTS public.subscription_plans (
    plan_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    free_trial BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    duration_days INTEGER NOT NULL,
    practice_question_limit INTEGER NOT NULL,
    mock_test_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mock_test_limit INTEGER,
    exam_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    exam_attempt_limit INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.plan_courses (
    plan_course_id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES public.subscription_plans(plan_id),
    course_id BIGINT NOT NULL REFERENCES public.courses(course_id),
    CONSTRAINT uq_plan_courses UNIQUE (plan_id, course_id)
);

CREATE TABLE IF NOT EXISTS public.user_subscriptions (
    subscription_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES public.users(user_id),
    plan_id BIGINT NOT NULL REFERENCES public.subscription_plans(plan_id),
    course_id BIGINT NOT NULL REFERENCES public.courses(course_id),
    starts_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    practice_questions_used INTEGER NOT NULL DEFAULT 0,
    mock_tests_used INTEGER NOT NULL DEFAULT 0,
    exam_attempts_used INTEGER NOT NULL DEFAULT 0
);

-- Upgrade installations created by an earlier subscription prototype.
ALTER TABLE public.subscription_plans
    ADD COLUMN IF NOT EXISTS description VARCHAR(255),
    ADD COLUMN IF NOT EXISTS free_trial BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS duration_days INTEGER NOT NULL DEFAULT 30,
    ADD COLUMN IF NOT EXISTS practice_question_limit INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS mock_test_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS mock_test_limit INTEGER,
    ADD COLUMN IF NOT EXISTS exam_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS exam_attempt_limit INTEGER,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE public.user_subscriptions
    ADD COLUMN IF NOT EXISTS starts_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS practice_questions_used INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS mock_tests_used INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS exam_attempts_used INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_plan_courses_course_id
    ON public.plan_courses(course_id);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_user_active
    ON public.user_subscriptions(user_id, active);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_course_active
    ON public.user_subscriptions(course_id, active);
