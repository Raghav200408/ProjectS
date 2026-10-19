-- Rule Engine Field Type references the selected Table Header by its unique name.
ALTER TABLE public.rule_engines
    ADD COLUMN IF NOT EXISTS field_type character varying(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'rule_engines_field_type_fkey'
          AND conrelid = 'public.rule_engines'::regclass
    ) THEN
        ALTER TABLE public.rule_engines
            ADD CONSTRAINT rule_engines_field_type_fkey
            FOREIGN KEY (field_type)
            REFERENCES public.table_headers (name);
    END IF;
END $$;
