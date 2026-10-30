BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'student_id'
          AND data_type IN ('smallint', 'integer', 'bigint', 'numeric')
    ) THEN
        ALTER TABLE public.users
            ALTER COLUMN student_id TYPE VARCHAR(50)
            USING student_id::text;
    END IF;
END
$$;

COMMIT;

SELECT
    table_schema,
    table_name,
    column_name,
    data_type,
    character_maximum_length
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'users'
  AND column_name = 'student_id';
