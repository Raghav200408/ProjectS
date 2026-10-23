-- One rule per main attribute ID per chapter ID, including inactive rules.
-- Run before deploying. This script never deletes or merges existing rules.
BEGIN;
LOCK TABLE public.rule_engines IN SHARE ROW EXCLUSIVE MODE;

-- Review these groups if the duplicate check below stops the migration.
SELECT chapter_id, attribute_id, COUNT(*) AS rule_count,
       ARRAY_AGG(rule_engine_id ORDER BY rule_engine_id) AS rule_engine_ids
FROM public.rule_engines
WHERE chapter_id IS NOT NULL AND attribute_id IS NOT NULL
GROUP BY chapter_id, attribute_id
HAVING COUNT(*) > 1
ORDER BY chapter_id, attribute_id;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.rule_engines
        WHERE chapter_id IS NOT NULL AND attribute_id IS NOT NULL
        GROUP BY chapter_id, attribute_id HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate chapter/attribute rules exist. Review the listed IDs before adding uniqueness. No rules were deleted or changed.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'rule_engines_chapter_attribute_key'
          AND conrelid = 'public.rule_engines'::regclass
    ) THEN
        ALTER TABLE public.rule_engines
            ADD CONSTRAINT rule_engines_chapter_attribute_key UNIQUE (chapter_id, attribute_id);
    END IF;
END $$;
COMMIT;
