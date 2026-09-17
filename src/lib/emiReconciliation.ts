export interface EMIScheduleRowItem {
  id?: string;
  booking_id?: string;
  installment_number: number;
  due_date: string;
  amount: number;
  paid_amount?: number;
  status?: "paid" | "partially_paid" | "pending" | "overdue" | string;
  notes?: string;
  account_type?: string;
}

export interface ReconciledScheduleResult {
  reconciledRows: EMIScheduleRowItem[];
  settledCount: number;
  partialCount: number;
  pendingCount: number;
  totalSettledAmount: number;
  remainingBalance: number;
  rebalancedEmiAmount: number;
  isFullyPaid: boolean;
}

export function addMonthsToDate(startDateStr: string, monthsToAdd: number, targetDayStr?: string): string {
  if (!startDateStr) return new Date().toISOString().slice(0, 10);
  try {
    const [y, m, d] = startDateStr.slice(0, 10).split("-").map(Number);
    const targetDay = targetDayStr ? parseInt(targetDayStr, 10) : d;

    const targetMonth = m - 1 + monthsToAdd;
    const newYear = y + Math.floor(targetMonth / 12);
    const newMonth = ((targetMonth % 12) + 12) % 12;

    const maxDays = new Date(newYear, newMonth + 1, 0).getDate();
    const finalDay = Math.min(targetDay || d || 5, maxDays);

    const formattedMonth = String(newMonth + 1).padStart(2, "0");
    const formattedDay = String(finalDay).padStart(2, "0");

    return `${newYear}-${formattedMonth}-${formattedDay}`;
  } catch {
    return startDateStr;
  }
}

/**
 * Reconciles EMI schedules with actual collected customer payments.
 * Directly reflects every actual payment made as a PAID row,
 * and auto-recalculates future pending terms to balance the remaining contract value.
 */
export function reconcileScheduleRows({
  rows = [],
  payments = [],
  totalPrice = 0,
  totalCollected = 0,
  startDate,
  recurrenceDay = "5",
  targetTotalTerms = 12,
  autoRebalancePending = true,
}: {
  rows?: EMIScheduleRowItem[];
  payments?: any[];
  totalPrice: number;
  totalCollected?: number;
  startDate?: string;
  recurrenceDay?: string;
  targetTotalTerms?: number;
  autoRebalancePending?: boolean;
}): ReconciledScheduleResult {
  const safeTotal = Math.max(0, Math.round(Number(totalPrice || 0)));

  // Calculate total from recorded payments list if provided
  const paymentsSum = (payments || []).reduce((sum, p) => sum + (Number(p.amount) || 0), 0);
  const safeCollected = Math.max(
    0,
    Math.round(Number(totalCollected || 0)),
    Math.round(paymentsSum)
  );

  const remainingBalance = Math.max(0, safeTotal - safeCollected);
  const isFullyPaid = remainingBalance <= 0 && safeTotal > 0;

  // 1. If individual payments exist, convert them directly into actual PAID rows
  if (payments && payments.length > 0) {
    // Sort payments chronologically
    const sortedPayments = [...payments].sort((a, b) => {
      const dateA = new Date(a.paid_on || a.created_at || 0).getTime();
      const dateB = new Date(b.paid_on || b.created_at || 0).getTime();
      return dateA - dateB;
    });

    const paidRows: EMIScheduleRowItem[] = sortedPayments.map((p, idx) => ({
      installment_number: idx + 1,
      due_date: p.paid_on ? p.paid_on.slice(0, 10) : new Date().toISOString().slice(0, 10),
      amount: Number(p.amount) || 0,
      paid_amount: Number(p.amount) || 0,
      status: "paid",
      notes: p.reference_number
        ? `Payment (${p.payment_method || "UPI"}: ${p.reference_number})`
        : `Payment Voucher #${idx + 1} (${p.payment_method || "UPI"})`,
    }));

    const futureRows: EMIScheduleRowItem[] = [];

    if (!isFullyPaid && remainingBalance > 0) {
      const totalTerms = Math.max(paidRows.length + 1, targetTotalTerms || 12);
      const pendingTermsCount = Math.max(1, totalTerms - paidRows.length);

      const baseShare = Math.floor(remainingBalance / pendingTermsCount);
      const remainder = remainingBalance - baseShare * pendingTermsCount;

      // Find first future due date (defaults to startDate or month after last payment)
      const futureStart =
        startDate ||
        addMonthsToDate(
          paidRows[paidRows.length - 1]?.due_date || new Date().toISOString().slice(0, 10),
          1,
          recurrenceDay
        );

      for (let i = 0; i < pendingTermsCount; i++) {
        const isLast = i === pendingTermsCount - 1;
        const rowAmt = isLast ? baseShare + remainder : baseShare;
        const termNum = paidRows.length + i + 1;
        const rowDueDate = addMonthsToDate(futureStart, i, recurrenceDay);

        futureRows.push({
          installment_number: termNum,
          due_date: rowDueDate,
          amount: rowAmt,
          paid_amount: 0,
          status: "pending",
          notes: `Scheduled EMI #${termNum}`,
        });
      }
    }

    const allReconciled = [...paidRows, ...futureRows];
    const settledCount = paidRows.length;
    const pendingCount = futureRows.length;
    const rebalancedEmiAmount = futureRows[0]?.amount || 0;

    return {
      reconciledRows: allReconciled,
      settledCount,
      partialCount: 0,
      pendingCount,
      totalSettledAmount: safeCollected,
      remainingBalance,
      rebalancedEmiAmount,
      isFullyPaid,
    };
  }

  // Fallback: If no payment records exist, manage standard schedule rows
  let currentRows: EMIScheduleRowItem[] = rows.length > 0
    ? [...rows].sort((a, b) => (a.installment_number || 0) - (b.installment_number || 0))
    : [];

  const termsCount = Math.max(1, currentRows.length || targetTotalTerms || 12);
  const start = startDate || (currentRows[0]?.due_date) || new Date().toISOString().slice(0, 10);

  if (currentRows.length === 0) {
    const perMonth = Math.floor(safeTotal / termsCount);
    const remainder = safeTotal - perMonth * termsCount;

    for (let i = 0; i < termsCount; i++) {
      const isLast = i === termsCount - 1;
      currentRows.push({
        installment_number: i + 1,
        due_date: addMonthsToDate(start, i, recurrenceDay),
        amount: isLast ? perMonth + remainder : perMonth,
        paid_amount: 0,
        status: "pending",
        notes: `Scheduled EMI #${i + 1}`,
      });
    }
  }

  let fundsPool = safeCollected;
  const processedRows: EMIScheduleRowItem[] = [];

  for (let i = 0; i < currentRows.length; i++) {
    const r = { ...currentRows[i], installment_number: i + 1 };
    const rowAmt = Number(r.amount) || 0;

    if (fundsPool >= rowAmt && rowAmt > 0) {
      r.paid_amount = rowAmt;
      r.status = "paid";
      fundsPool -= rowAmt;
    } else if (fundsPool > 0) {
      r.paid_amount = fundsPool;
      r.status = "partially_paid";
      fundsPool = 0;
    } else {
      r.paid_amount = 0;
      r.status = "pending";
    }
    processedRows.push(r);
  }

  const unpaidRows = processedRows.filter((r) => r.status !== "paid");
  let rebalancedEmiAmount = 0;

  if (isFullyPaid) {
    processedRows.forEach((r) => {
      r.paid_amount = Number(r.amount) || 0;
      r.status = "paid";
    });
  } else if (autoRebalancePending && unpaidRows.length > 0 && remainingBalance > 0) {
    const count = unpaidRows.length;
    const baseShare = Math.floor(remainingBalance / count);
    const remainder = remainingBalance - baseShare * count;
    rebalancedEmiAmount = baseShare;

    unpaidRows.forEach((r, pos) => {
      const isLast = pos === count - 1;
      r.amount = isLast ? baseShare + remainder : baseShare;
      r.paid_amount = 0;
      r.status = "pending";
    });
  }

  const settledCount = processedRows.filter((r) => r.status === "paid").length;
  const partialCount = processedRows.filter((r) => r.status === "partially_paid").length;
  const pendingCount = processedRows.filter((r) => r.status === "pending").length;
  const totalSettledAmount = processedRows.reduce(
    (sum, r) => sum + (r.status === "paid" ? Number(r.amount) : Number(r.paid_amount || 0)),
    0
  );

  return {
    reconciledRows: processedRows,
    settledCount,
    partialCount,
    pendingCount,
    totalSettledAmount,
    remainingBalance,
    rebalancedEmiAmount,
    isFullyPaid,
  };
}
