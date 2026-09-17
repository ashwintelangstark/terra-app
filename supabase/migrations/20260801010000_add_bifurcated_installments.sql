-- Add collection_account, govt_component, and company_component columns to installment_payments
ALTER TABLE public.installment_payments
  ADD COLUMN IF NOT EXISTS collection_account TEXT DEFAULT 'company',
  ADD COLUMN IF NOT EXISTS govt_component NUMERIC(14,2) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS company_component NUMERIC(14,2) DEFAULT 0;
