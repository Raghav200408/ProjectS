-- Course-specific curated question access for subscription plans.
-- Run against PostgreSQL before deploying the backend changes.

ALTER TABLE plan_courses
    ADD COLUMN IF NOT EXISTS question_limit INTEGER,
    ADD COLUMN IF NOT EXISTS all_course_questions BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS plan_questions (
    plan_question_id BIGSERIAL PRIMARY KEY,
    plan_course_id BIGINT NOT NULL REFERENCES plan_courses(plan_course_id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(question_id) ON DELETE CASCADE,
    CONSTRAINT uq_plan_questions_plan_course_question UNIQUE (plan_course_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_plan_questions_question_id ON plan_questions(question_id);
