-- ============================================================
-- Mock-exam module schema.
--
-- Mirrors the exam tables but for practice papers: a mock exam is
-- scoped only to a course + its chapters (no college / branch /
-- section, no start / end window). Run this once against the
-- Hintmatrix DB before deploying the matching backend build -
-- there is no ddl-auto / Flyway in this project, so schema changes
-- are manual.
-- ============================================================

BEGIN;

-- 1. The mock exam itself.
CREATE TABLE IF NOT EXISTS public.mock_exam (
    mock_exam_id    BIGSERIAL PRIMARY KEY,
    mock_exam_name  VARCHAR(255) NOT NULL,
    course_id       BIGINT       NOT NULL,
    pass_percentage INTEGER      NOT NULL,
    active_row      BOOLEAN      DEFAULT TRUE,
    row_status      INTEGER      DEFAULT 1,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT fk_mock_exam_course
        FOREIGN KEY (course_id) REFERENCES public.courses (course_id)
);

-- 2. Chapters attached to a mock exam (many-to-many).
CREATE TABLE IF NOT EXISTS public.mock_exam_chapters (
    mock_exam_id BIGINT NOT NULL,
    chapter_id   BIGINT NOT NULL,
    PRIMARY KEY (mock_exam_id, chapter_id),
    CONSTRAINT fk_mock_exam_chapters_mock_exam
        FOREIGN KEY (mock_exam_id) REFERENCES public.mock_exam (mock_exam_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_mock_exam_chapters_chapter
        FOREIGN KEY (chapter_id) REFERENCES public.chapters (chapter_id)
);

-- 3. Questions added to a mock exam.
CREATE TABLE IF NOT EXISTS public.mock_exam_questions (
    mock_exam_question_id BIGSERIAL PRIMARY KEY,
    mock_exam_id          BIGINT NOT NULL,
    question_id           BIGINT NOT NULL,
    created_at            TIMESTAMP,
    CONSTRAINT uk_mock_exam_question UNIQUE (mock_exam_id, question_id),
    CONSTRAINT fk_mock_exam_questions_mock_exam
        FOREIGN KEY (mock_exam_id) REFERENCES public.mock_exam (mock_exam_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_mock_exam_questions_question
        FOREIGN KEY (question_id) REFERENCES public.questions (question_id)
);

-- 4. A candidate's result for one mock-exam attempt.
CREATE TABLE IF NOT EXISTS public.mock_exam_result (
    mock_exam_result_id BIGSERIAL PRIMARY KEY,
    mock_exam_id        BIGINT           NOT NULL,
    user_id             BIGINT           NOT NULL,
    total_marks         DOUBLE PRECISION NOT NULL,
    percentage          DOUBLE PRECISION NOT NULL,
    created_at          TIMESTAMP,
    CONSTRAINT fk_mock_exam_result_mock_exam
        FOREIGN KEY (mock_exam_id) REFERENCES public.mock_exam (mock_exam_id),
    CONSTRAINT fk_mock_exam_result_user
        FOREIGN KEY (user_id) REFERENCES public.users (user_id)
);

COMMIT;
