-- Add govt_amount and company_amount columns to bookings for deal price bifurcation tracking
ALTER TABLE public.bookings
  ADD COLUMN IF NOT EXISTS govt_amount NUMERIC(14,2),
  ADD COLUMN IF NOT EXISTS company_amount NUMERIC(14,2);
