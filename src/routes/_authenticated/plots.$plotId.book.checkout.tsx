import { useState, useEffect } from "react";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import {
  ArrowLeft,
  BadgeIndianRupee,
  CalendarDays,
  CheckCircle2,
  ShieldCheck,
  UserRound,
  Sparkles,
  Building,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AmountInput } from "@/components/common/AmountInput";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import type { LeadRow } from "@/components/site-mapper/types";
import { sendBookingConfirmationWhatsApp } from "@/lib/whatsappService";
import { sanitizePhoneInput, getPhoneValidationError, formatPhoneWithCountryCode } from "@/lib/phoneValidation";

export const Route = createFileRoute("/_authenticated/plots/$plotId/book/checkout")({
  validateSearch: (search: Record<string, unknown>): { leadId?: string } => ({
    leadId: typeof search.leadId === "string" ? search.leadId : undefined,
  }),
  component: BookingCheckout,
});

function BookingCheckout() {
  const { plotId } = Route.useParams();
  const { leadId } = Route.useSearch();
  const { user } = Route.useRouteContext();
  const nav = useNavigate();
  const qc = useQueryClient();

  const [form, setForm] = useState({
    customer_name: "",
    customer_phone: "",
    customer_email: "",
    customer_address: "",
    govt_value: "",
    finalPrice: "",
    advancePaid: "",
    installments: "12",
    customInstallments: "",
    firstDueDate: "",
    paymentMethod: "UPI",
    remarks: "",
  });

  const { data: plot } = useQuery({
    queryKey: ["plot", plotId],
    queryFn: async () =>
      (await supabase.from("plots").select("*, projects(name, code)").eq("id", plotId).maybeSingle()).data as any,
  });

  const { data: lead } = useQuery({
    queryKey: ["lead", leadId],
    enabled: !!leadId,
    queryFn: async () =>
      (await (supabase as any).from("plot_leads").select("*").eq("id", leadId).maybeSingle()).data as LeadRow | null,
  });

  const ownerId = lead?.created_by ?? lead?.assigned_to;
  const { data: owner } = useQuery({
    queryKey: ["booking-owner", ownerId],
    enabled: !!ownerId,
    queryFn: async () =>
      (await supabase.from("profiles").select("id, full_name, phone, email, job_title").eq("id", ownerId!).maybeSingle()).data,
  });

  useEffect(() => {
    if (lead) {
      setForm((current) => ({
        ...current,
        customer_name: lead.name,
        customer_phone: sanitizePhoneInput(lead.phone || ""),
        customer_email: lead.email ?? "",
        customer_address: current.customer_address || (lead as any)?.address || "",
        remarks: lead.notes ?? "",
        finalPrice: current.finalPrice || String(plot?.price ?? ""),
        govt_value: current.govt_value || String((plot as any)?.govt_value ?? (plot as any)?.govt_amount ?? ""),
      }));
    } else if (plot) {
      setForm((current) => ({
        ...current,
        finalPrice: current.finalPrice || String(plot.price ?? ""),
        govt_value: current.govt_value || String((plot as any)?.govt_value ?? (plot as any)?.govt_amount ?? ""),
      }));
    }
  }, [lead, plot, ownerId]);

  const set = (key: keyof typeof form, value: string) =>
    setForm((current) => ({ ...current, [key]: value }));

  const money = (value: number) => `₹${value.toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;

  const listedPrice = Number(plot?.price ?? 0);
  const finalPrice = Number(form.finalPrice || listedPrice);
  const advancePaid = Number(form.advancePaid || 0);
  const balance = Math.max(finalPrice - advancePaid, 0);
  const installmentCount = Math.max(
    Number(form.installments === "custom" ? form.customInstallments : form.installments) || 1,
    1
  );
  const perInstallment = installmentCount > 0 ? balance / installmentCount : balance;
  const concession = Math.max(listedPrice - finalPrice, 0);
  const calculatedIncentive = (finalPrice * Number(plot?.incentive_percentage ?? 0)) / 100;

  const mutation = useMutation({
    mutationFn: async () => {
      if (!lead || !ownerId) throw new Error("This lead has no recorded owner. Assign an employee before booking.");

      if (!form.customer_name.trim()) throw new Error("Please enter customer name.");
      const phoneErr = getPhoneValidationError(form.customer_phone);
      if (phoneErr) throw new Error(phoneErr);
      if (finalPrice <= 0) throw new Error("Please enter a valid final price.");

      // Check if an active booking pipeline already exists for this plot
      const { data: existingActive } = await (supabase as any)
        .from("bookings")
        .select("id, status, customer_name")
        .eq("plot_id", plotId)
        .in("status", ["pending", "approved", "booked"])
        .order("created_at", { ascending: false })
        .limit(1)
        .maybeSingle();

      if (existingActive) {
        throw new Error(
          `An active booking already exists for this plot (Customer: ${existingActive.customer_name}).`
        );
      }

      const formattedPhone = formatPhoneWithCountryCode(form.customer_phone);

      // 1. Create Booking Record
      const bookingPayload: any = {
        plot_id: plotId,
        lead_id: lead.id,
        customer_name: form.customer_name.trim(),
        customer_phone: formattedPhone,
        customer_email: form.customer_email.trim() || null,
        customer_address: form.customer_address.trim() || null,
        total_price: finalPrice,
        booking_amount: advancePaid > 0 ? advancePaid : finalPrice,
        advance_paid: advancePaid,
        govt_value: Number(form.govt_value || 0),
        govt_amount: Number(form.govt_value || 0),
        payment_method: form.paymentMethod,
        booking_date: new Date().toISOString().slice(0, 10),
        sales_executive_id: ownerId || user.id,
        status: "approved",
        remarks: form.remarks.trim() || null,
        created_by: user.id,
      };

      let { data: booking, error: bookingErr } = await (supabase as any)
        .from("bookings")
        .insert(bookingPayload)
        .select()
        .single();

      if (bookingErr && (bookingErr.code === "PGRST204" || bookingErr.message?.includes("schema cache"))) {
        // Automatic resilient fallback if optional columns missing in remote DB
        const fallbackPayload = { ...bookingPayload };
        delete fallbackPayload.govt_value;
        delete fallbackPayload.govt_amount;
        delete fallbackPayload.balance_amount;
        delete fallbackPayload.installment_count;
        delete fallbackPayload.installment_amount;
        delete fallbackPayload.first_installment_due_date;
        delete fallbackPayload.attribution_type;
        delete fallbackPayload.bdo_id;
        delete fallbackPayload.external_bdo_name;

        const res = await (supabase as any)
          .from("bookings")
          .insert(fallbackPayload)
          .select()
          .single();

        booking = res.data;
        bookingErr = res.error;
      }

      if (bookingErr) throw bookingErr;

      // 2. If Advance Paid > 0, record initial payment voucher
      if (advancePaid > 0) {
        await (supabase as any).from("installment_payments").insert({
          booking_id: booking.id,
          amount: advancePaid,
          payment_method: form.paymentMethod,
          paid_on: new Date().toISOString().slice(0, 10),
          recorded_by: user.id,
          notes: "Initial advance booking payment",
        });
      }

      // 3. Generate Initial Installment Schedule Rows
      const scheduleRows: any[] = [];
      let nextDue = form.firstDueDate || new Date().toISOString().slice(0, 10);

      // If advance paid, record it as first settled EMI item
      if (advancePaid > 0) {
        scheduleRows.push({
          booking_id: booking.id,
          installment_number: 1,
          due_date: new Date().toISOString().slice(0, 10),
          amount: advancePaid,
          paid_amount: advancePaid,
          status: "paid",
          notes: "Initial Advance Booking Payment",
        });
      }

      // Generate remaining installment terms
      if (balance > 0 && installmentCount > 0) {
        const baseShare = Math.floor(balance / installmentCount);
        const remainder = balance - baseShare * installmentCount;
        const startIdx = advancePaid > 0 ? 2 : 1;

        for (let i = 0; i < installmentCount; i++) {
          const isLast = i === installmentCount - 1;
          const amt = isLast ? baseShare + remainder : baseShare;

          // Simple date month increment
          const [y, m, d] = nextDue.split("-").map(Number);
          const targetMonth = m - 1 + i;
          const newYear = y + Math.floor(targetMonth / 12);
          const newMonth = String(((targetMonth % 12) + 12) % 12 + 1).padStart(2, "0");
          const finalDay = String(Math.min(d || 5, new Date(newYear, Number(newMonth), 0).getDate())).padStart(2, "0");
          const rowDueDate = `${newYear}-${newMonth}-${finalDay}`;

          scheduleRows.push({
            booking_id: booking.id,
            installment_number: startIdx + i,
            due_date: rowDueDate,
            amount: amt,
            paid_amount: 0,
            status: "pending",
            notes: `Scheduled EMI #${startIdx + i}`,
          });
        }
      }

      if (scheduleRows.length > 0) {
        await (supabase as any).from("booking_installment_schedules").insert(scheduleRows);
      }

      // 4. Update plot status to "booked"
      await (supabase as any)
        .from("plots")
        .update({ status: "booked" })
        .eq("id", plotId);

      // 5. Convert lead status to "converted"
      await (supabase as any)
        .from("plot_leads")
        .update({ status: "converted" })
        .eq("id", lead.id);

      // 6. Record activity log
      await (supabase as any).from("lead_activities").insert({
        lead_id: lead.id,
        activity_type: "converted",
        from_status: lead.status,
        to_status: "converted",
        notes: `Lead successfully converted to booking for Plot #${plot?.plot_number ?? "Site"}${
          plot?.projects?.name ? ` in ${plot.projects.name}` : ""
        } at final price ${money(finalPrice)}`,
        performed_by: user.id,
      });

      // 7. Send WhatsApp Confirmation
      try {
        await sendBookingConfirmationWhatsApp({
          customerName: form.customer_name.trim(),
          customerPhone: formattedPhone,
          projectName: plot?.projects?.name || "Project",
          plotNumber: String(plot?.plot_number || "Site"),
          totalPrice: finalPrice,
          bookingAmountPaid: advancePaid,
          bookingDate: new Date().toISOString().slice(0, 10),
          salesExecutiveName: owner?.full_name || undefined,
        });
      } catch (waErr) {
        console.warn("WhatsApp notification attempt:", waErr);
      }
    },
    onSuccess: () => {
      qc.invalidateQueries();
      toast.success("Booking confirmed successfully!");
      nav({ to: "/bookings" });
    },
    onError: (error: any) => {
      toast.error(error.message ?? "Unable to create booking");
    },
  });

  if (!leadId) {
    nav({ to: "/plots/$plotId/book", params: { plotId }, replace: true });
    return null;
  }

  return (
    <div className="h-[calc(100vh-5rem)] min-h-[650px] flex flex-col overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between pb-5 shrink-0">
        <div className="flex items-center gap-4">
          <Link
            to="/plots/$plotId/book"
            params={{ plotId }}
            className="rounded-xl border p-2 text-muted-foreground hover:text-foreground hover:bg-muted"
          >
            <ArrowLeft className="h-4 w-4" />
          </Link>
          <div>
            <p className="text-xs uppercase tracking-[0.18em] text-muted-foreground">
              Booking studio · Step 2 of 2
            </p>
            <h1 className="text-display text-3xl mt-1">Confirm the deal</h1>
          </div>
        </div>

        <div className="hidden md:flex items-center gap-2 text-xs text-emerald-700 dark:text-emerald-400 font-medium">
          <ShieldCheck className="h-4 w-4" /> Lead ownership protected
        </div>
      </div>

      {/* Main 2-Column Form Layout */}
      <div className="grid xl:grid-cols-[minmax(0,1fr)_385px] gap-5 flex-1 min-h-0">
        {/* Left Column: Form Fields */}
        <section className="rounded-2xl border bg-card p-6 overflow-y-auto space-y-6 shadow-xs">
          {/* 1. Customer Profile */}
          <div className="space-y-4">
            <div className="flex items-center gap-2 pb-2 border-b">
              <UserRound className="h-4 w-4 text-terracotta" />
              <h2 className="font-bold text-base">Customer Profile</h2>
              <span className="text-xs text-muted-foreground ml-auto">Auto-filled from lead</span>
            </div>

            <div className="grid sm:grid-cols-2 gap-4">
              <Field label="Full Name *">
                <Input
                  value={form.customer_name}
                  onChange={(e) => set("customer_name", e.target.value)}
                  placeholder="e.g. Ramesh Kulkarni"
                  className="rounded-xl"
                  required
                />
              </Field>

              <Field label="Phone Number (10 digits) *">
                <div className="relative flex items-center">
                  <span className="inline-flex items-center px-3 h-9 rounded-l-xl border border-r-0 border-input bg-muted/60 text-xs font-mono font-bold text-muted-foreground select-none">
                    +91
                  </span>
                  <Input
                    type="tel"
                    maxLength={10}
                    value={form.customer_phone}
                    onChange={(e) => set("customer_phone", sanitizePhoneInput(e.target.value))}
                    placeholder="9876543210"
                    className="rounded-l-none font-mono text-xs font-bold rounded-r-xl"
                    required
                  />
                </div>
                {form.customer_phone && form.customer_phone.length < 10 && (
                  <p className="text-[10px] text-amber-600 mt-1">
                    Enter {10 - form.customer_phone.length} more digit{10 - form.customer_phone.length > 1 ? "s" : ""}
                  </p>
                )}
              </Field>

              <Field label="Email Address">
                <Input
                  type="email"
                  value={form.customer_email}
                  onChange={(e) => set("customer_email", e.target.value)}
                  placeholder="name@example.com"
                  className="rounded-xl"
                />
              </Field>

              <Field label="Payment Method">
                <Select value={form.paymentMethod} onValueChange={(value) => set("paymentMethod", value)}>
                  <SelectTrigger className="rounded-xl">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="rounded-xl">
                    <SelectItem value="UPI">UPI</SelectItem>
                    <SelectItem value="Bank transfer">Bank Transfer (NEFT / RTGS)</SelectItem>
                    <SelectItem value="Cheque">Cheque</SelectItem>
                    <SelectItem value="Cash">Cash</SelectItem>
                  </SelectContent>
                </Select>
              </Field>

              <Field label="Customer Address" className="sm:col-span-2">
                <Textarea
                  rows={2}
                  value={form.customer_address}
                  onChange={(e) => set("customer_address", e.target.value)}
                  placeholder="Complete postal address"
                  className="rounded-xl"
                />
              </Field>
            </div>
          </div>

          {/* 2. Price & Payment Plan */}
          <div className="space-y-4 pt-2">
            <div className="flex items-center gap-2 pb-2 border-b">
              <BadgeIndianRupee className="h-4 w-4 text-terracotta" />
              <h2 className="font-bold text-base">Price & Payment Plan</h2>
            </div>

            <div className="grid sm:grid-cols-2 gap-4">
              <Field label="Registered Plot Price">
                <Input
                  type="text"
                  value={money(listedPrice)}
                  readOnly
                  className="bg-muted/60 text-muted-foreground font-mono font-bold cursor-not-allowed rounded-xl"
                />
              </Field>

              <Field label="Govt / Guideline Value (₹)">
                <AmountInput
                  value={form.govt_value}
                  onChangeValue={(numVal) => set("govt_value", numVal ? numVal.toString() : "")}
                  placeholder="e.g. 20,00,000"
                  className="font-mono font-bold rounded-xl"
                />
              </Field>

              <Field label="Negotiated Final Agreed Price *">
                <AmountInput
                  value={form.finalPrice}
                  onChangeValue={(numVal) => set("finalPrice", numVal ? numVal.toString() : "")}
                  placeholder="e.g. 35,00,000"
                  className="font-mono font-bold rounded-xl"
                  required
                />
              </Field>

              <Field label="Concession Made">
                <Input
                  value={concession > 0 ? `− ${money(concession)}` : "No concession"}
                  readOnly
                  className={`font-mono font-semibold cursor-not-allowed rounded-xl ${
                    concession > 0
                      ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-300 border-emerald-500/30"
                      : "bg-muted/60 text-muted-foreground"
                  }`}
                />
              </Field>

              <Field label="Paid Today (Advance Amount)">
                <AmountInput
                  value={form.advancePaid}
                  onChangeValue={(numVal) => set("advancePaid", numVal ? numVal.toString() : "")}
                  placeholder="e.g. 5,00,000"
                  className="font-mono font-bold rounded-xl"
                />
              </Field>

              <Field label="Installments Count">
                <Select value={form.installments} onValueChange={(value) => set("installments", value)}>
                  <SelectTrigger className="rounded-xl">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="rounded-xl">
                    {[1, 3, 6, 9, 12, 18, 24, 36].map((count) => (
                      <SelectItem key={count} value={String(count)}>
                        {count} {count === 1 ? "Installment (Single payment)" : "Monthly Installments"}
                      </SelectItem>
                    ))}
                    <SelectItem value="custom">Custom Months…</SelectItem>
                  </SelectContent>
                </Select>
              </Field>

              {form.installments === "custom" && (
                <Field label="Custom Installment Months">
                  <Input
                    type="number"
                    min="1"
                    step="1"
                    placeholder="e.g. 15"
                    value={form.customInstallments}
                    onChange={(e) => set("customInstallments", e.target.value.replace(/[^0-9]/g, ""))}
                    className="rounded-xl font-mono"
                  />
                </Field>
              )}

              <Field label="First Installment Due (Optional)">
                <Input
                  type="date"
                  value={form.firstDueDate}
                  onChange={(e) => set("firstDueDate", e.target.value)}
                  className="rounded-xl"
                />
                <p className="text-[11px] text-muted-foreground mt-1">
                  Optional: can also be configured anytime under Installments.
                </p>
              </Field>

              <Field label="Internal Notes" className="sm:col-span-2">
                <Input
                  value={form.remarks}
                  onChange={(e) => set("remarks", e.target.value)}
                  placeholder="Special deal terms or payment instructions..."
                  className="rounded-xl"
                />
              </Field>
            </div>
          </div>
        </section>

        {/* Right Sidebar: Deal Summary */}
        <aside className="rounded-2xl border bg-card p-6 flex flex-col justify-between overflow-y-auto shadow-xs space-y-5">
          <div className="space-y-4">
            <div className="flex items-center gap-2 pb-3 border-b">
              <CheckCircle2 className="h-5 w-5 text-terracotta" />
              <h2 className="font-bold text-lg">Deal Summary</h2>
            </div>

            <div className="space-y-3 text-sm">
              <Summary
                label="Plot"
                value={`${plot?.projects?.name ?? "Project"} · Plot #${plot?.plot_number ?? "Site"}`}
              />
              <Summary label="Registered Price" value={money(listedPrice)} />
              {Number(form.govt_value || 0) > 0 && (
                <Summary label="Govt / Guideline Value" value={money(Number(form.govt_value))} />
              )}
              <Summary
                label="Concession"
                value={concession > 0 ? `− ${money(concession)}` : "—"}
                highlight={concession > 0}
              />

              <div className="rounded-2xl bg-terracotta/[0.08] border border-terracotta/20 p-4">
                <p className="text-xs text-muted-foreground font-semibold uppercase tracking-wider">
                  Final Agreed Price
                </p>
                <p className="text-3xl text-display font-extrabold text-foreground mt-1">
                  {money(finalPrice)}
                </p>
              </div>

              <Summary label="Paid Today (Advance)" value={money(advancePaid)} highlight={advancePaid > 0} />
              <Summary label="Remaining Balance" value={money(balance)} />

              <div className="rounded-xl border border-dashed p-3.5 bg-muted/20">
                <div className="flex items-center gap-2.5">
                  <CalendarDays className="h-4 w-4 text-terracotta shrink-0" />
                  <div>
                    <p className="text-[11px] text-muted-foreground uppercase font-bold tracking-wider">
                      EMI Schedule Plan
                    </p>
                    <p className="font-bold text-sm text-foreground mt-0.5">
                      {installmentCount} × {money(perInstallment)}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Lead Owner Attribution Info */}
            <div className="pt-4 border-t space-y-2">
              <p className="text-[10px] uppercase tracking-wider font-bold text-muted-foreground">
                Assigned Sales Executive
              </p>
              <div className="rounded-xl bg-muted/40 p-3 text-xs space-y-1">
                <p className="font-bold text-foreground">{owner?.full_name ?? "Sales Executive"}</p>
                <p className="text-muted-foreground">
                  {owner?.job_title ?? "Sales Executive"}
                  {owner?.phone ? ` · ${owner.phone}` : ""}
                </p>
                {calculatedIncentive > 0 && (
                  <p className="text-[11px] text-emerald-700 dark:text-emerald-400 font-semibold pt-1">
                    ★ Eligible for {money(calculatedIncentive)} incentive ({plot?.incentive_percentage ?? 0}%)
                  </p>
                )}
              </div>
            </div>
          </div>

          <Button
            type="button"
            disabled={
              mutation.isPending ||
              !lead ||
              !form.customer_name.trim() ||
              !form.customer_phone.trim() ||
              form.customer_phone.length < 10 ||
              finalPrice <= 0 ||
              (form.installments === "custom" && !form.customInstallments)
            }
            onClick={() => mutation.mutate()}
            className="w-full bg-terracotta text-accent-foreground hover:bg-terracotta/90 h-11 text-sm font-bold shadow-md rounded-xl cursor-pointer"
          >
            {mutation.isPending ? "Confirming Booking…" : "Confirm Booking"}
          </Button>
        </aside>
      </div>
    </div>
  );
}

function Field({
  label,
  className,
  children,
}: {
  label: string;
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <div className={className}>
      <Label className="text-xs font-semibold text-muted-foreground">{label}</Label>
      <div className="mt-1.5">{children}</div>
    </div>
  );
}

function Summary({
  label,
  value,
  highlight,
}: {
  label: string;
  value: string;
  highlight?: boolean;
}) {
  return (
    <div className="flex justify-between gap-3 text-xs">
      <span className="text-muted-foreground font-medium">{label}</span>
      <span
        className={
          highlight
            ? "text-emerald-700 dark:text-emerald-400 font-bold"
            : "font-semibold text-right"
        }
      >
        {value}
      </span>
    </div>
  );
}
