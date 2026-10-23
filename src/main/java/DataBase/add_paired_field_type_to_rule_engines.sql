-- Run before deploying the backend that maps paired_field_type.
BEGIN;
ALTER TABLE public.rule_engines
    ADD COLUMN IF NOT EXISTS paired_field_type character varying(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'rule_engines_paired_field_type_fkey'
          AND conrelid = 'public.rule_engines'::regclass
    ) THEN
        ALTER TABLE public.rule_engines
            ADD CONSTRAINT rule_engines_paired_field_type_fkey
            FOREIGN KEY (paired_field_type) REFERENCES public.table_headers(name);
    END IF;
END $$;

-- Backfill each existing rule from its own paired attribute, regardless of the main header.
UPDATE public.rule_engines r
SET paired_field_type = h.name
FROM public.table_attributes a
JOIN public.table_headers h ON h.header_id = a.header_id
WHERE a.attribute_id = r.pair_attribute_id
  AND NULLIF(TRIM(r.paired_field_type), '') IS NULL;
COMMIT;
