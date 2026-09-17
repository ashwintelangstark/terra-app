import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { syncTransferToTally, syncTransferRepaymentToTally } from "@/lib/tallySync";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
  Landmark,
  ArrowRightLeft,
  Calendar,
  Building2,
  FileSpreadsheet,
  Zap,
  CheckCircle2,
  Clock,
  IndianRupee,
  ShieldCheck,
} from "lucide-react";
import { toast } from "sonner";

export interface TreasuryTallyLedgerModalProps {
  transfer: any | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function TreasuryTallyLedgerModal({
  transfer,
  open,
  onOpenChange,
}: TreasuryTallyLedgerModalProps) {
  const [syncing, setSyncing] = useState(false);

  // Fetch repayments for this specific transfer
  const { data: repayments = [], refetch: refetchRepayments } = useQuery({
    queryKey: ["treasury-repayments", transfer?.id],
    enabled: !!transfer?.id && open,
    queryFn: async () => {
      const { data, error } = await (supabase as any)
        .from("project_transfer_repayments")
        .select("*")
        .eq("transfer_id", transfer.id)
        .order("created_at", { ascending: true });
      if (error) return [];
      return data ?? [];
    },
  });

  const formatMoney = (val: number) =>
    `₹${Number(val || 0).toLocaleString("en-IN", { maximumFractionDigits: 2 })}`;

  if (!transfer) return null;

  const srcProj = transfer.source_project?.name || "Source Project";
  const tgtProj = transfer.target_project?.name || "Target Project";
  const amt = Number(transfer.amount || 0);
  const repaidAmt = Number(transfer.repaid_amount || 0);
  const remaining = Math.max(0, amt - repaidAmt);
  const pct = amt > 0 ? Math.min(100, Math.round((repaidAmt / amt) * 100)) : 0;
  const trfRef = `TRF-${transfer.id?.slice(0, 6)?.toUpperCase() || "2026-101"}`;

  const handleSyncToTally = async () => {
    setSyncing(true);
    try {
      // 1. Sync Initial Transfer Journal Voucher
      const syncRes = await syncTransferToTally({
        sourceProject: srcProj,
        targetProject: tgtProj,
        amount: amt,
        transferDate: transfer.created_at?.slice(0, 10),
        transferRef: trfRef,
      });

      if (!syncRes.success) {
        toast.error(`Tally Initial Transfer Sync: ${syncRes.responseText}`);
        setSyncing(false);
        return;
      }

      // 2. Sync All Repayments
      let syncedRepaymentsCount = 0;
      if (repayments.length > 0) {
        for (let i = 0; i < repayments.length; i++) {
          const r = repayments[i];
          const repRef = `REP-${trfRef}-${String(i + 1).padStart(2, "0")}`;
          await syncTransferRepaymentToTally({
            sourceProject: srcProj,
            targetProject: tgtProj,
            amount: Number(r.amount),
            repaymentDate: r.created_at?.slice(0, 10),
            repaymentRef: repRef,
          });
          syncedRepaymentsCount++;
        }
      }

      toast.success(
        `Synced 1 Transfer Journal + ${syncedRepaymentsCount} Repayment Vouchers for ${trfRef} into Tally Prime!`
      );
      refetchRepayments();
    } catch (err: any) {
      toast.error(`Tally sync error: ${err.message || "Failed to reach Tally Prime on port 9000"}`);
    } finally {
      setSyncing(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="w-[96vw] max-w-5xl max-h-[92vh] overflow-y-auto overflow-x-hidden bg-card p-5 md:p-7 gap-5 shadow-2xl border border-border rounded-2xl">
        <DialogHeader className="border-b border-border/60 pb-4 space-y-2">
          <div className="flex items-center justify-between gap-3 flex-wrap">
            <div className="flex items-center gap-2 flex-wrap">
              <Badge variant="outline" className="font-mono text-xs uppercase bg-terracotta/10 text-terracotta border-terracotta/30 px-2.5 py-0.5">
                {trfRef}
              </Badge>
              <Badge variant="secondary" className="font-bold text-xs px-2.5 py-0.5">
                Journal Voucher
              </Badge>
              <Badge
                variant="outline"
                className={`text-xs font-semibold px-2.5 py-0.5 ${
                  pct >= 100
                    ? "bg-emerald-500/10 text-emerald-600 border-emerald-500/30"
                    : "bg-amber-500/10 text-amber-600 border-amber-500/30"
                }`}
              >
                {pct >= 100 ? "100% Repaid" : `${pct}% Repaid`}
              </Badge>
            </div>

            <Button
              size="sm"
              variant="default"
              onClick={handleSyncToTally}
              disabled={syncing}
              className="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs gap-2 px-4 shadow-sm"
            >
              <Zap className={`h-4 w-4 ${syncing ? "animate-spin" : ""}`} />
              {syncing ? "Syncing Journal to Tally..." : "Sync Journal Voucher to Tally"}
            </Button>
          </div>

          <div>
            <DialogTitle className="text-2xl font-extrabold text-foreground tracking-tight flex items-center gap-2">
              Treasury Tally Statement — {srcProj} <ArrowRightLeft className="h-5 w-5 text-terracotta" /> {tgtProj}
            </DialogTitle>
            <DialogDescription className="text-xs text-muted-foreground mt-0.5">
              Inter-project capital transfer accounting ledger detailing journal voucher entries and repayment tracking.
            </DialogDescription>
          </div>
        </DialogHeader>

        {/* Transfer Metrics Cards */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="p-3.5 rounded-xl bg-muted/40 border border-border/60 space-y-1">
            <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground flex items-center gap-1">
              <Building2 className="h-3.5 w-3.5 text-terracotta" /> Source Project
            </span>
            <span className="font-bold text-sm text-foreground block truncate">{srcProj}</span>
            <span className="text-[11px] text-muted-foreground">Debited: {srcProj} Collection Bank A/c</span>
          </div>

          <div className="p-3.5 rounded-xl bg-muted/40 border border-border/60 space-y-1">
            <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground flex items-center gap-1">
              <Building2 className="h-3.5 w-3.5 text-emerald-600" /> Target Project
            </span>
            <span className="font-bold text-sm text-foreground block truncate">{tgtProj}</span>
            <span className="text-[11px] text-muted-foreground">Credited Capital Fund</span>
          </div>

          <div className="p-3.5 rounded-xl bg-emerald-500/5 border border-emerald-500/20 space-y-1">
            <span className="text-[10px] uppercase font-bold tracking-wider text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
              <IndianRupee className="h-3.5 w-3.5" /> Transfer Amount
            </span>
            <span className="font-extrabold text-sm text-foreground block">{formatMoney(amt)}</span>
            <span className="text-[11px] text-emerald-600 font-semibold">{formatMoney(repaidAmt)} Repaid</span>
          </div>

          <div className="p-3.5 rounded-xl bg-amber-500/5 border border-amber-500/20 space-y-1">
            <span className="text-[10px] uppercase font-bold tracking-wider text-amber-600 dark:text-amber-400 flex items-center gap-1">
              <Clock className="h-3.5 w-3.5" /> Remaining Repayment
            </span>
            <span className="font-extrabold text-sm text-amber-600 dark:text-amber-400 block">{formatMoney(remaining)}</span>
            <span className="text-[11px] text-muted-foreground">Pending Recovery</span>
          </div>
        </div>

        {/* Repayment Progress Bar */}
        <div className="space-y-1.5 px-0.5">
          <div className="flex justify-between text-xs font-bold">
            <span className="text-muted-foreground">Capital Repayment Recovery</span>
            <span className="text-emerald-600 dark:text-emerald-400">{formatMoney(repaidAmt)} of {formatMoney(amt)} ({pct}%)</span>
          </div>
          <Progress value={pct} className="h-2.5 bg-muted" />
        </div>

        {/* Journal Voucher Table */}
        <div className="space-y-2 w-full">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-bold uppercase tracking-wider text-foreground flex items-center gap-1.5">
              <FileSpreadsheet className="h-4 w-4 text-terracotta" /> Double-Entry Tally Journal Breakdown
            </h4>
            <Badge variant="outline" className="text-[11px] font-mono px-2.5">
              {1 + repayments.length} Journal Entries
            </Badge>
          </div>

          <div className="rounded-xl border border-border/80 overflow-hidden bg-card shadow-xs w-full">
            <table className="w-full text-xs text-left border-collapse table-auto">
              <thead>
                <tr className="bg-muted/70 border-b border-border/80 uppercase text-[10px] tracking-wider text-muted-foreground font-bold">
                  <th className="py-3 px-3">Date</th>
                  <th className="py-3 px-3">Vch Type</th>
                  <th className="py-3 px-3">Reference ID</th>
                  <th className="py-3 px-3">Debit Ledger (Dr)</th>
                  <th className="py-3 px-3">Credit Ledger (Cr)</th>
                  <th className="py-3 px-3 text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60 font-mono">
                {/* Initial Capital Transfer Journal Voucher */}
                <tr className="hover:bg-muted/40 transition-colors">
                  <td className="py-3 px-3 font-sans text-muted-foreground whitespace-nowrap">
                    {transfer.created_at?.slice(0, 10) || "2026-07-31"}
                  </td>
                  <td className="py-3 px-3 font-sans whitespace-nowrap">
                    <Badge variant="outline" className="text-[10px] font-bold px-2 py-0.5 bg-blue-500/10 text-blue-600 border-blue-500/30">
                      Journal
                    </Badge>
                  </td>
                  <td className="py-3 px-3 font-bold text-foreground whitespace-nowrap">{trfRef}</td>
                  <td className="py-3 px-3 font-sans font-bold text-foreground">
                    {tgtProj} Collection Bank A/c
                  </td>
                  <td className="py-3 px-3 font-sans text-muted-foreground">
                    {srcProj} Collection Bank A/c
                  </td>
                  <td className="py-3 px-3 text-right font-extrabold text-foreground whitespace-nowrap">
                    {formatMoney(amt)}
                  </td>
                </tr>

                {/* Repayments */}
                {repayments.map((r: any, idx: number) => (
                  <tr key={r.id} className="hover:bg-muted/40 transition-colors">
                    <td className="py-3 px-3 font-sans text-muted-foreground whitespace-nowrap">
                      {r.created_at?.slice(0, 10) || "2026-07-31"}
                    </td>
                    <td className="py-3 px-3 font-sans whitespace-nowrap">
                      <Badge variant="outline" className="text-[10px] font-bold px-2 py-0.5 bg-emerald-500/10 text-emerald-600 border-emerald-500/30">
                        Repayment
                      </Badge>
                    </td>
                    <td className="py-3 px-3 font-bold text-foreground whitespace-nowrap">
                      {`REP-${trfRef}-${String(idx + 1).padStart(2, "0")}`}
                    </td>
                    <td className="py-3 px-3 font-sans text-emerald-600 dark:text-emerald-400 font-bold">
                      {srcProj} Collection Bank A/c
                    </td>
                    <td className="py-3 px-3 font-sans text-muted-foreground">
                      {tgtProj} Collection Bank A/c
                    </td>
                    <td className="py-3 px-3 text-right font-bold text-emerald-600 dark:text-emerald-400 whitespace-nowrap">
                      {formatMoney(Number(r.amount))}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Tally Prime Inspection Guide Box */}
        <div className="p-4 rounded-xl bg-gradient-to-r from-terracotta/10 via-amber-500/10 to-card border border-terracotta/20 space-y-2 text-xs">
          <div className="flex items-center gap-2 text-terracotta font-bold text-sm">
            <Landmark className="h-4 w-4" /> How to View this Inter-Project Journal Voucher inside Tally Prime:
          </div>
          <div className="text-muted-foreground space-y-1.5 pl-5 list-decimal font-medium">
            <p>1. Open **Tally Prime** ➔ Press <kbd className="px-2 py-0.5 bg-muted rounded border text-[11px] font-mono">Alt + G</kbd> (Go To).</p>
            <p>2. Type **`Day Book`** or **`Journal Register`** and press **Enter**.</p>
            <p>3. You will see the **Journal Voucher** recorded under <strong className="text-foreground">{tgtProj} Collection Bank A/c</strong> for <strong className="text-emerald-600">{formatMoney(amt)}</strong>!</p>
          </div>
        </div>

        <div className="flex justify-end pt-1">
          <Button variant="outline" size="sm" onClick={() => onOpenChange(false)} className="text-xs px-5">
            Close Statement
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
