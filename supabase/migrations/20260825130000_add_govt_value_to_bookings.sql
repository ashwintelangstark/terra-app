-- Add govt_value (Guideline / Government Value) and extended booking fields to bookings and plots tables
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS govt_value NUMERIC DEFAULT 0;
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS govt_amount NUMERIC DEFAULT 0;
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS balance_amount NUMERIC DEFAULT 0;
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS installment_count INTEGER DEFAULT 1;
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS installment_amount NUMERIC DEFAULT 0;
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS first_installment_due_date DATE;
ALTER TABLE public.plots ADD COLUMN IF NOT EXISTS govt_value NUMERIC DEFAULT 0;


