-- Apply once when deploying the existing Final Accounts drag-and-drop update.
-- Idempotent, and intentionally leaves other chapters and custom rule definitions alone.
BEGIN;

ALTER TABLE public.question_answers ADD COLUMN IF NOT EXISTS question_attribute_id bigint;
ALTER TABLE public.answer_events ADD COLUMN IF NOT EXISTS question_attribute_id bigint;

UPDATE public.rule_engines r
SET active_row = true, updated_at = CURRENT_TIMESTAMP
FROM public.chapters c, public.table_attributes a, public.table_names t, public.table_headers h
WHERE r.chapter_id = c.chapter_id AND r.attribute_id = a.attribute_id
  AND r.table1_id = t.table_name_id AND r.header1_id = h.header_id
  AND lower(trim(c.name)) IN ('final accounts without adjustments', 'final accounts with adjustments')
  AND lower(trim(a.name)) = 'wages'
  AND lower(trim(t.name)) = 'trading account' AND lower(trim(h.name)) = 'debit particulars'
  AND lower(trim(r.arithmetic1)) = 'add' AND r.active_row = false;

UPDATE public.rule_engines r
SET header1_id = asset.header_id, updated_at = CURRENT_TIMESTAMP
FROM public.chapters c, public.table_attributes a, public.table_names t,
     public.table_headers wrong, public.table_headers asset
WHERE r.chapter_id = c.chapter_id AND r.attribute_id = a.attribute_id
  AND r.table1_id = t.table_name_id AND r.header1_id = wrong.header_id
  AND lower(trim(c.name)) IN ('final accounts without adjustments', 'final accounts with adjustments')
  AND lower(trim(a.name)) = 'plant & machinery' AND lower(trim(t.name)) = 'balance sheet'
  AND lower(trim(wrong.name)) = 'debit particulars' AND lower(trim(asset.name)) = 'asset side';

UPDATE public.rule_engines r
SET amount_position1 = '1', updated_at = CURRENT_TIMESTAMP
FROM public.chapters c, public.table_attributes a, public.table_names t
WHERE r.chapter_id = c.chapter_id AND r.attribute_id = a.attribute_id AND r.table1_id = t.table_name_id
  AND lower(trim(c.name)) IN ('final accounts without adjustments', 'final accounts with adjustments')
  AND lower(trim(a.name)) = 'purchase returns' AND lower(trim(t.name)) = 'trading account'
  AND r.amount_position1 = '0';

UPDATE public.rule_engines r
SET pair_attribute_id = base.attribute_id, updated_at = CURRENT_TIMESTAMP
FROM public.chapters c, public.table_attributes a, public.table_attributes base, public.table_names t
WHERE r.chapter_id = c.chapter_id AND r.attribute_id = a.attribute_id AND r.table1_id = t.table_name_id
  AND lower(trim(c.name)) IN ('final accounts without adjustments', 'final accounts with adjustments')
  AND lower(trim(t.name)) = 'trading account' AND lower(trim(r.arithmetic1)) = 'less'
  AND (r.pair_attribute_id IS NULL OR r.pair_attribute_id = r.attribute_id)
  AND ((lower(trim(a.name)) = 'sales returns' AND lower(trim(base.name)) = 'sales')
       OR (lower(trim(a.name)) = 'purchase returns' AND lower(trim(base.name)) = 'purchases'));

COMMIT;
